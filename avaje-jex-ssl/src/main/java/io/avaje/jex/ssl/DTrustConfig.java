package io.avaje.jex.ssl;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

class DTrustConfig implements TrustConfig {

  private final List<Certificate> certificates = new ArrayList<>();
  private final List<KeyStore> keyStores = new ArrayList<>();
  private final Function<String, InputStream> loader;

  DTrustConfig(Function<String, InputStream> loader) {
    this.loader = loader;
  }

  @Override
  public TrustConfig certificateFromClasspath(String certificateFile) {
    return certificateFromInputStream(loader.apply(certificateFile));
  }

  @Override
  public TrustConfig certificateFromInputStream(InputStream certificateInputStream) {

    certificates.addAll(KeyStoreUtil.parseCertificates(certificateInputStream));

    return this;
  }

  @Override
  public TrustConfig certificateFromPath(String certificatePath) {
    try (InputStream is = Files.newInputStream(Paths.get(certificatePath))) {
      certificates.addAll(KeyStoreUtil.parseCertificates(is));
    } catch (Exception e) {
      throw new SslConfigException("Failed to load certificates from path: " + certificatePath, e);
    }
    return this;
  }

  public List<Certificate> certificates() {
    return certificates;
  }

  public List<KeyStore> keyStores() {
    return keyStores;
  }

  @Override
  public TrustConfig p7bCertificateFromString(String certificateString) {

    certificates.addAll(KeyStoreUtil.parseCertificates(certificateString));

    return this;
  }

  @Override
  public TrustConfig pemFromString(String cert) {
    certificates.addAll(KeyStoreUtil.parseCertificates(cert));
    return this;
  }

  /**
   * Load a trust store from the classpath.
   *
   * @param trustStoreFile The name of the trust store file in the classpath.
   * @param trustStorePassword password for the trust store.
   */
  @Override
  public TrustConfig trustStoreFromClasspath(String trustStoreFile, String trustStorePassword) {
    return trustStoreFromInputStream(loader.apply(trustStoreFile), trustStorePassword);
  }

  /**
   * Load a trust store from a given input stream. The trust store can be in JKS or PKCS12 format.
   *
   * @param trustStoreInputStream input stream to the trust store file.
   * @param trustStorePassword password for the trust store.
   */
  @Override
  public TrustConfig trustStoreFromInputStream(
      InputStream trustStoreInputStream, String trustStorePassword) {

    keyStores.add(
        KeyStoreUtil.loadKeyStore(trustStoreInputStream, trustStorePassword.toCharArray()));

    return this;
  }

  /**
   * Load a trust store from a given path in the system. The trust store can be in JKS or PKCS12
   * format.
   *
   * @param trustStorePath path to the trust store file.
   * @param trustStorePassword password for the trust store.
   */
  @Override
  public TrustConfig trustStoreFromPath(String trustStorePath, String trustStorePassword) {
    try (InputStream is = Files.newInputStream(Paths.get(trustStorePath))) {
      keyStores.add(KeyStoreUtil.loadKeyStore(is, trustStorePassword.toCharArray()));
    } catch (Exception e) {
      throw new SslConfigException("Failed to load trust store from path: " + trustStorePath, e);
    }
    return this;
  }
}
