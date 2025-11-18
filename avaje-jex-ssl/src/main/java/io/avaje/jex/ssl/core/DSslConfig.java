package io.avaje.jex.ssl.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.Provider;
import java.util.function.Consumer;

import io.avaje.jex.spi.ClassResourceLoader;
import io.avaje.jex.ssl.SslConfig;
import io.avaje.jex.ssl.SslConfigException;
import io.avaje.jex.ssl.TrustConfig;

final class DSslConfig implements SslConfig {

  private static final String MULTIPLE_IDENTITY =
      "Both the certificate and key must be provided using the same method";

  private String identityPassword;
  private KeyStore keyStore = null;
  private boolean loadedIdentity;
  private Provider securityProvider = null;
  private DTrustConfig trustConfig = null;
  private ClassResourceLoader resourceLoader = ClassResourceLoader.fromClass(SslConfig.class);

  String identityPassword() {
    return identityPassword;
  }

  KeyStore keyStore() {
    return keyStore;
  }

  @Override
  public void keystoreFromClasspath(
      String keyStoreFile, String keyStorePassword, String identityPassword) {
    keystoreFromInputStream(loadCP(keyStoreFile), keyStorePassword, identityPassword);
  }

  @Override
  public void keystoreFromInputStream(
      InputStream keyStoreInputStream, String keyStorePassword, String identityPassword) {
    setKeyStore(KeyStoreUtil.loadKeyStore(keyStoreInputStream, keyStorePassword.toCharArray()));
    this.identityPassword = identityPassword != null ? identityPassword : keyStorePassword;
  }

  @Override
  public void keystoreFromPath(
      String keyStorePath, String keyStorePassword, String identityPassword) {
    try {
      var path = Paths.get(keyStorePath);
      setKeyStore(
          KeyStoreUtil.loadKeyStore(Files.newInputStream(path), keyStorePassword.toCharArray()));
      this.identityPassword = identityPassword != null ? identityPassword : keyStorePassword;
    } catch (IOException e) {
      throw new SslConfigException("Failed to load keystore from path: " + keyStorePath, e);
    }
  }

  private InputStream loadCP(String path) {
    InputStream url = null;
    try {
      url = resourceLoader.loadResource(path).openStream();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return url;
  }

  boolean loadedIdentity() {
    return loadedIdentity;
  }

  @Override
  public void pemFromClasspath(String certificateFile, String privateKeyFile, String password) {
    pemFromInputStream(loadCP(certificateFile), loadCP(privateKeyFile), password);
  }

  @Override
  public void pemFromInputStream(
      InputStream certificateInputStream, InputStream privateKeyInputStream, String password) {
    try {
      var keyContent = new String(privateKeyInputStream.readAllBytes());

      setKeyStore(
          KeyStoreUtil.loadIdentityFromPem(
              certificateInputStream,
              keyContent,
              password != null ? password.toCharArray() : null));
      this.identityPassword = identityPassword != null ? identityPassword : "";

    } catch (IOException e) {
      throw new SslConfigException("Failed to read PEM content from streams", e);
    }
  }

  @Override
  public void pemFromPath(String certificatePath, String privateKeyPath, String password) {
    try (var certContent = Files.newInputStream(Paths.get(certificatePath))) {

      var keyPath = Paths.get(privateKeyPath);

      var keyContent = Files.readString(keyPath);
      setKeyStore(
          KeyStoreUtil.loadIdentityFromPem(
              certContent, keyContent, password != null ? password.toCharArray() : null));
      this.identityPassword = identityPassword != null ? identityPassword : "";
    } catch (IOException e) {
      throw new SslConfigException("Failed to read PEM files", e);
    }
  }

  @Override
  public void pemFromString(String certificateString, String privateKeyString, String password) {
    setKeyStore(
        KeyStoreUtil.loadIdentityFromPem(
            new ByteArrayInputStream(certificateString.getBytes(StandardCharsets.UTF_8)),
            privateKeyString,
            password != null ? password.toCharArray() : null));
    this.identityPassword = identityPassword != null ? identityPassword : "";
  }

  @Override
  public SslConfig resourceLoader(Class<?> clazz) {
    resourceLoader = ClassResourceLoader.fromClass(clazz);
    return this;
  }

  Provider securityProvider() {
    return securityProvider;
  }

  @Override
  public void securityProvider(Provider securityProvider) {
    this.securityProvider = securityProvider;
  }

  private void setKeyStore(KeyStore keyStore) {
    if (loadedIdentity) {
      throw new SslConfigException(MULTIPLE_IDENTITY);
    }
    if (keyStore != null) {
      loadedIdentity = true;
      this.keyStore = keyStore;
    }
  }

  DTrustConfig trustConfig() {
    return trustConfig;
  }

  @Override
  public void withTrustConfig(Consumer<TrustConfig> trustConfigConsumer) {
    trustConfig = trustConfig == null ? new DTrustConfig(this::loadCP) : trustConfig;
    trustConfigConsumer.accept(trustConfig);
  }
}