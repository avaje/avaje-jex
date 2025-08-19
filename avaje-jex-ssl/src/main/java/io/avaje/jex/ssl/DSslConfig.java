package io.avaje.jex.ssl;

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

import javax.net.ssl.X509ExtendedKeyManager;

import io.avaje.jex.spi.ClassResourceLoader;

/** Data class to hold the configuration for the plugin. */
final class DSslConfig implements SslConfig {

  private static final String MULTIPLE_IDENTITY =
      "Both the certificate and key must be provided using the same method";

  enum LoadedIdentity {
    KEY_MANAGER,
    KEY_STORE,
    NONE
  }

  private String identityPassword;
  private X509ExtendedKeyManager keyManager = null;
  private KeyStore keyStore = null;
  private LoadedIdentity loadedIdentity = LoadedIdentity.NONE;
  private Provider securityProvider = null;
  private DTrustConfig trustConfig = null;
  private ClassResourceLoader resourceLoader = ClassResourceLoader.fromClass(SslConfig.class);

  public KeyStore keyStore() {
    return keyStore;
  }

  String identityPassword() {
    return identityPassword;
  }

  public X509ExtendedKeyManager keyManager() {
    return keyManager;
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

  LoadedIdentity loadedIdentity() {
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
      setKeyManager(
          KeyStoreUtil.loadIdentityFromPem(
              certificateInputStream,
              keyContent,
              password != null ? password.toCharArray() : null));
    } catch (IOException e) {
      throw new SslConfigException("Failed to read PEM content from streams", e);
    }
  }

  @Override
  public void pemFromPath(String certificatePath, String privateKeyPath, String password) {
    try (var certContent = Files.newInputStream(Paths.get(certificatePath))) {

      var keyPath = Paths.get(privateKeyPath);

      var keyContent = Files.readString(keyPath);
      setKeyManager(
          KeyStoreUtil.loadIdentityFromPem(
              certContent, keyContent, password != null ? password.toCharArray() : null));
    } catch (IOException e) {
      throw new SslConfigException("Failed to read PEM files", e);
    }
  }

  @Override
  public void pemFromString(String certificateString, String privateKeyString, String password) {
    setKeyManager(
        KeyStoreUtil.loadIdentityFromPem(
            new ByteArrayInputStream(certificateString.getBytes(StandardCharsets.UTF_8)),
            privateKeyString,
            password != null ? password.toCharArray() : null));
  }

  Provider securityProvider() {
    return securityProvider;
  }

  @Override
  public void securityProvider(Provider securityProvider) {
    this.securityProvider = securityProvider;
  }

  private void setKeyManager(X509ExtendedKeyManager keyManager) {
    if (loadedIdentity != LoadedIdentity.NONE) {
      throw new SslConfigException(MULTIPLE_IDENTITY);
    }
    if (keyManager != null) {
      loadedIdentity = LoadedIdentity.KEY_MANAGER;
      this.keyManager = keyManager;
    }
  }

  private void setKeyStore(KeyStore keyStore) {
    if (loadedIdentity != LoadedIdentity.NONE) {
      throw new SslConfigException(MULTIPLE_IDENTITY);
    }
    if (keyStore != null) {
      loadedIdentity = LoadedIdentity.KEY_STORE;
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
