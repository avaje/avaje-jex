package io.avaje.jex.ssl.core;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.avaje.jex.ssl.SslConfigException;
import io.avaje.jex.ssl.TrustConfig;

final class DTrustConfig implements TrustConfig {

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
    try (var is = Files.newInputStream(Paths.get(certificatePath))) {
      certificates.addAll(KeyStoreUtil.parseCertificates(is));
    } catch (Exception e) {
      throw new SslConfigException("Failed to load certificates from path: " + certificatePath, e);
    }
    return this;
  }

  List<Certificate> certificates() {
    return certificates;
  }

  List<KeyStore> keyStores() {
    return keyStores;
  }

  @Override
  public TrustConfig certificateFromString(String cert) {
    certificates.addAll(KeyStoreUtil.parseCertificates(cert));
    return this;
  }

  @Override
  public TrustConfig trustStoreFromClasspath(String trustStoreFile, String trustStorePassword) {
    return trustStoreFromInputStream(loader.apply(trustStoreFile), trustStorePassword);
  }

  @Override
  public TrustConfig trustStoreFromInputStream(InputStream trustStoreInputStream, String trustStorePassword) {
    keyStores.add(KeyStoreUtil.loadKeyStore(trustStoreInputStream, trustStorePassword.toCharArray()));
    return this;
  }

  @Override
  public TrustConfig trustStoreFromPath(String trustStorePath, String trustStorePassword) {
    try (var is = Files.newInputStream(Paths.get(trustStorePath))) {
      keyStores.add(KeyStoreUtil.loadKeyStore(is, trustStorePassword.toCharArray()));
    } catch (Exception e) {
      throw new SslConfigException("Failed to load trust store from path: " + trustStorePath, e);
    }
    return this;
  }
}
