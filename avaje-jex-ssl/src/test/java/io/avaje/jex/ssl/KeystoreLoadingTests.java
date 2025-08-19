package io.avaje.jex.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.avaje.jex.spi.ClassResourceLoader;
import io.avaje.jex.ssl.cert.Server;

@Tag("integration")
class KeystoreLoadingTests extends IntegrationTestClass {

  static ClassResourceLoader loader = ClassResourceLoader.fromClass(KeystoreLoadingTests.class);

  //////////////////////////////
  // Valid keystore loading   //
  //////////////////////////////

  @Test
  void loadingValidJksKeystoreFromClasspath() {
    assertSslWorks(
        config ->
            config.keystoreFromClasspath(Server.JKS_KEY_STORE_NAME, Server.KEY_STORE_PASSWORD));
  }

  @Test
  void loadingValidP12KeystoreFromClasspath() {
    assertSslWorks(
        config ->
            config.keystoreFromClasspath(Server.P12_KEY_STORE_NAME, Server.KEY_STORE_PASSWORD));
  }

  @Test
  void loadingValidJksKeystoreFromPath() {
    assertSslWorks(
        config -> config.keystoreFromPath(Server.JKS_KEY_STORE_PATH, Server.KEY_STORE_PASSWORD));
  }

  @Test
  void loadingValidP12KeystoreFromPath() {
    assertSslWorks(
        config -> config.keystoreFromPath(Server.P12_KEY_STORE_PATH, Server.KEY_STORE_PASSWORD));
  }

  @Test
  void loadingValidJksKeystoreFromInputStream() {
    assertSslWorks(
        config ->
            config.keystoreFromInputStream(
                Server.JKS_KEY_STORE_INPUT_STREAM_SUPPLIER.get(), Server.KEY_STORE_PASSWORD));
  }

  @Test
  void loadingValidP12KeystoreFromInputStream() {
    assertSslWorks(
        config ->
            config.keystoreFromInputStream(
                Server.P12_KEY_STORE_INPUT_STREAM_SUPPLIER.get(), Server.KEY_STORE_PASSWORD));
  }

  // ------------------------------------------------------------------------------------------------------------------

  //////////////////////////////
  // Invalid keystore loading //
  //////////////////////////////

  @Test
  void loadingMissingJksKeystoreFromClasspathFails() {
    Assertions.assertThrows(
        NullPointerException.class,
        () -> {
          assertSslWorks(
              config -> config.keystoreFromClasspath("/invalid", Server.KEY_STORE_PASSWORD));
        });
  }

  @Test
  void loadingJksKeystoreFromClasspathWithInvalidPasswordFails() {
    Assertions.assertThrows(
        SslConfigException.class,
        () -> {
          assertSslWorks(
              config -> config.keystoreFromClasspath(Server.JKS_KEY_STORE_NAME, "invalid"));
        });
  }

  @Test
  void loadingP12KeystoreFromClasspathWithInvalidPasswordFails() {
    Assertions.assertThrows(
        SslConfigException.class,
        () -> {
          assertSslWorks(
              config -> config.keystoreFromClasspath(Server.P12_KEY_STORE_NAME, "invalid"));
        });
  }

  @Test
  void loadingMissingJksKeystoreFromPathFails() {
    Assertions.assertThrows(
        SslConfigException.class,
        () -> {
          assertSslWorks(config -> config.keystoreFromPath("invalid", Server.KEY_STORE_PASSWORD));
        });
  }

  @Test
  void loadingJksKeystoreFromPathWithInvalidPasswordFails() {
    Assertions.assertThrows(
        SslConfigException.class,
        () -> {
          assertSslWorks(config -> config.keystoreFromPath(Server.JKS_KEY_STORE_PATH, "invalid"));
        });
  }

  @Test
  void loadingP12KeystoreFromPathWithInvalidPasswordFails() {
    Assertions.assertThrows(
        SslConfigException.class,
        () -> {
          assertSslWorks(config -> config.keystoreFromPath(Server.P12_KEY_STORE_PATH, "invalid"));
        });
  }

  @Test
  void loadingMissingJksKeystoreFromInputStreamFails() {
    Assertions.assertThrows(
        SslConfigException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.keystoreFromInputStream(
                      InputStream.nullInputStream(), Server.KEY_STORE_PASSWORD));
        });
  }

  @Test
  void loadingJksKeystoreFromInputStreamWithInvalidPasswordFails() {
    Assertions.assertThrows(
        SslConfigException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.keystoreFromInputStream(
                      Server.JKS_KEY_STORE_INPUT_STREAM_SUPPLIER.get(), "invalid"));
        });
  }

  @Test
  void loadingP12KeystoreFromInputStreamWithInvalidPasswordFails() {
    Assertions.assertThrows(
        SslConfigException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.keystoreFromInputStream(
                      Server.P12_KEY_STORE_INPUT_STREAM_SUPPLIER.get(), "invalid"));
        });
  }

  @Test
  void loadingMalformedJksKeystoreFromClasspathFails() {
    Assertions.assertThrows(
        SslConfigException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.keystoreFromClasspath(MALFORMED_JKS_FILE_NAME, Server.KEY_STORE_PASSWORD));
        });
  }

  @Test
  void loadingMalformedP12KeystoreFromClasspathFails() {
    Assertions.assertThrows(
        SslConfigException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.keystoreFromClasspath(MALFORMED_P12_FILE_NAME, Server.KEY_STORE_PASSWORD));
        });
  }

  @Test
  void loadingMalformedJksKeystoreFromPathFails() {
    Assertions.assertThrows(
        SslConfigException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.keystoreFromPath(MALFORMED_JKS_FILE_PATH, Server.KEY_STORE_PASSWORD));
        });
  }

  @Test
  void loadingMalformedP12KeystoreFromPathFails() {
    Assertions.assertThrows(
        SslConfigException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.keystoreFromPath(MALFORMED_P12_FILE_PATH, Server.KEY_STORE_PASSWORD));
        });
  }

  @Test
  void loadingMalformedJksKeystoreFromInputStreamFails() {
    Assertions.assertThrows(
        SslConfigException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.keystoreFromInputStream(
                      MALFORMED_JKS_INPUT_STREAM_SUPPLIER.get(), Server.KEY_STORE_PASSWORD));
        });
  }

  @Test
  void loadingMalformedP12KeystoreFromInputStreamFails() {
    Assertions.assertThrows(
        SslConfigException.class,
        () -> {
          assertSslWorks(
              config ->
                  config.keystoreFromInputStream(
                      MALFORMED_P12_INPUT_STREAM_SUPPLIER.get(), Server.KEY_STORE_PASSWORD));
        });
  }

  private static final String MALFORMED_JKS_FILE_NAME = "test-certs/server/malformed.jks";
  private static final String MALFORMED_P12_FILE_NAME = "test-certs/server/malformed.p12";
  private static String MALFORMED_JKS_FILE_PATH;
  private static String MALFORMED_P12_FILE_PATH;

  private static final Supplier<InputStream> MALFORMED_JKS_INPUT_STREAM_SUPPLIER =
      () -> {
        try {
          return loader.loadResource(MALFORMED_JKS_FILE_NAME).openStream();
        } catch (IOException e) {
          e.printStackTrace();
        }
        return null;
      };

  private static final Supplier<InputStream> MALFORMED_P12_INPUT_STREAM_SUPPLIER =
      () -> {
        try {
          return loader.loadResource(MALFORMED_P12_FILE_NAME).openStream();
        } catch (IOException e) {
          e.printStackTrace();
        }
        return null;
      };

  static {
    try {
      MALFORMED_JKS_FILE_PATH =
          Path.of(loader.loadResource(MALFORMED_JKS_FILE_NAME).toURI()).toAbsolutePath().toString();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    try {
      MALFORMED_P12_FILE_PATH =
          Path.of(loader.loadResource(MALFORMED_P12_FILE_NAME).toURI()).toAbsolutePath().toString();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
