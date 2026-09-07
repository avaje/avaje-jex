package io.avaje.jex.ssl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.jupiter.api.Test;

import io.avaje.jex.ssl.cert.Server;

/**
 * Directly exercises {@code io.avaje.jex.ssl.core.PemAPISupport} - the class that decodes PEM
 * content. Pre-JDK 28 it does manual regex/Base64/DER parsing (which can't decrypt encrypted
 * private keys); the JDK 28 multi-release override (see {@code src/main/java28}) instead uses the
 * finalized {@code java.security.PEMDecoder} API (JEP 542), which can.
 *
 * <p>Maven Surefire runs tests against the exploded {@code target/classes} directory rather than
 * the packaged multi-release jar, so referencing {@code PemAPISupport} normally would always hit
 * the pre-JDK-28 implementation regardless of which JDK runs the build. To actually exercise the
 * real JDK 28 override, this test assembles a small multi-release jar from the already-compiled
 * class files (both the base implementation and, when present, the {@code META-INF/versions/28}
 * override) and loads it via an isolated {@link URLClassLoader}, which triggers the JVM's genuine
 * multi-release jar resolution based on the running JDK version.
 */
class PemApiSupportTests {

  private static final String CLASS_NAME = "io.avaje.jex.ssl.core.PemAPISupport";

  private static Method certificatesMethod;
  private static Method privateKeyMethod;

  private static void load() throws Exception {
    if (certificatesMethod != null) {
      return;
    }
    var jarFile = buildIsolatedJar();
    var loader =
        new URLClassLoader(
            new URL[] {jarFile.toURI().toURL()}, ClassLoader.getPlatformClassLoader());
    var pemApiSupport = loader.loadClass(CLASS_NAME);

    certificatesMethod = pemApiSupport.getDeclaredMethod("certificates", String.class);
    certificatesMethod.setAccessible(true);

    privateKeyMethod = pemApiSupport.getDeclaredMethod("privateKey", String.class, char[].class);
    privateKeyMethod.setAccessible(true);
  }

  /**
   * Mirrors the whole compiled {@code target/classes} tree into a jar - same layout Maven's {@code
   * jar:jar} goal produces, including {@code META-INF/versions/28} when this build ran on JDK 28+
   * (so the jar is genuinely multi-release only then). Copying everything, rather than just {@code
   * PemAPISupport.class}, keeps this working regardless of what else that class ends up depending
   * on (e.g. {@code SslConfigException}).
   */
  private static File buildIsolatedJar() throws Exception {
    var classesDir =
        new File(SslConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI());

    var manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    manifest.getMainAttributes().putValue("Multi-Release", "true");

    var jarFile = File.createTempFile("pem-api-support", ".jar");
    jarFile.deleteOnExit();

    try (var jos = new JarOutputStream(Files.newOutputStream(jarFile.toPath()), manifest)) {
      var classesPath = classesDir.toPath();
      try (var stream = Files.walk(classesPath)) {
        for (var file : (Iterable<Path>) stream.filter(Files::isRegularFile)::iterator) {
          var entryPath = classesPath.relativize(file).toString().replace(File.separatorChar, '/');
          if ("module-info.class".equals(entryPath)) {
            continue;
          }
          jos.putNextEntry(new JarEntry(entryPath));
          try (InputStream in = Files.newInputStream(file)) {
            jos.write(readAll(in));
          }
          jos.closeEntry();
        }
      }
    }
    return jarFile;
  }

  private static byte[] readAll(InputStream in) throws Exception {
    var out = new ByteArrayOutputStream();
    in.transferTo(out);
    return out.toByteArray();
  }

  private static boolean runningOnJdk28Plus() {
    return Runtime.version().feature() >= 28;
  }

  @Test
  void privateKey_encryptedWithCorrectPassword() throws Exception {
    load();

    if (runningOnJdk28Plus()) {
      var result =
          privateKeyMethod.invoke(
              null, Server.ENCRYPTED_KEY_AS_STRING, Server.KEY_PASSWORD.toCharArray());
      assertNotNull(result, "JDK 28+ should decrypt the PEM key via PEMDecoder");
      assertEquals("RSA", ((PrivateKey) result).getAlgorithm());
    } else {
      var ex =
          assertThrows(
              InvocationTargetException.class,
              () ->
                  privateKeyMethod.invoke(
                      null, Server.ENCRYPTED_KEY_AS_STRING, Server.KEY_PASSWORD.toCharArray()));
      assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
    }
  }

  @Test
  void privateKey_encryptedWithWrongPassword() throws Exception {
    load();
    var ex =
        assertThrows(
            InvocationTargetException.class,
            () ->
                privateKeyMethod.invoke(
                    null, Server.ENCRYPTED_KEY_AS_STRING, "wrong".toCharArray()));
    assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
  }

  @Test
  void privateKey_unencrypted() throws Exception {
    load();

    var result = privateKeyMethod.invoke(null, Server.NON_ENCRYPTED_KEY_AS_STRING, null);

    assertNotNull(result, "both implementations can parse an unencrypted PKCS8 key");
    assertEquals("RSA", ((PrivateKey) result).getAlgorithm());
  }

  @Test
  @SuppressWarnings("unchecked")
  void certificates_singleAndMulti() throws Exception {
    load();

    var single = (List<Certificate>) certificatesMethod.invoke(null, Server.CERTIFICATE_AS_STRING);
    var multi =
        (List<Certificate>)
            certificatesMethod.invoke(
                null, Server.CERTIFICATE_AS_STRING + "\n" + Server.NORWAY_CERTIFICATE_AS_STRING);

    assertEquals(1, single.size());
    assertEquals(2, multi.size());
  }
}
