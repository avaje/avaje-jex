package io.avaje.jex.ssl;

import java.io.InputStream;
import java.security.Provider;
import java.util.function.Consumer;

/**
 * The `SslConfig` interface provides a fluent API for configuring SSL/TLS settings. It supports
 * loading key stores and PEM-formatted identity data from various sources such as file paths, input
 * streams, and the classpath.
 *
 * <p>This interface is designed to simplify the setup of secure communication by offering default
 * methods that streamline common use cases.
 */
public interface SslConfig {

  /**
   * Load a key store from the classpath.
   *
   * @param keyStoreFile name of the key store file in the classpath.
   * @param keyStorePassword password for the key store.
   */
  default void keystoreFromClasspath(String keyStoreFile, String keyStorePassword) {
    keystoreFromClasspath(keyStoreFile, keyStorePassword, null);
  }

  /**
   * Load a key store from the classpath.
   *
   * @param keyStoreFile name of the key store file in the classpath.
   * @param keyStorePassword password for the key store.
   * @param identityPassword password for the identity, if different from the key store password.
   */
  void keystoreFromClasspath(String keyStoreFile, String keyStorePassword, String identityPassword);

  /**
   * Load a key store from a given input stream.
   *
   * @param keyStoreInputStream input stream to the key store file.
   * @param keyStorePassword password for the key store
   */
  default void keystoreFromInputStream(InputStream keyStoreInputStream, String keyStorePassword) {
    keystoreFromInputStream(keyStoreInputStream, keyStorePassword, null);
  }

  /**
   * Load a key store from a given input stream.
   *
   * @param keyStoreInputStream input stream to the key store file.
   * @param keyStorePassword password for the key store
   * @param identityPassword password for the identity, if different from the key store password.
   */
  void keystoreFromInputStream(
      InputStream keyStoreInputStream, String keyStorePassword, String identityPassword);

  /**
   * Load a key store from a given path in the system.
   *
   * @param keyStorePath path to the key store file.
   * @param keyStorePassword password for the key store.
   */
  default void keystoreFromPath(String keyStorePath, String keyStorePassword) {
    keystoreFromPath(keyStorePath, keyStorePassword, null);
  }

  /**
   * Load a key store from a given path in the system.
   *
   * @param keyStorePath path to the key store file.
   * @param keyStorePassword password for the key store.
   * @param identityPassword password for the identity, if different from the key store password.
   */
  void keystoreFromPath(String keyStorePath, String keyStorePassword, String identityPassword);

  /**
   * Load pem formatted identity data from the classpath.
   *
   * @param certificateFile The name of the pem certificate file in the classpath.
   * @param privateKeyFile The name of the pem private key file in the classpath.
   */
  default void pemFromClasspath(String certificateFile, String privateKeyFile) {
    pemFromClasspath(certificateFile, privateKeyFile, null);
  }
  /**
   * Load pem formatted identity data from the classpath.
   *
   * @param certificateFile The name of the pem certificate file in the classpath.
   * @param privateKeyFile The name of the pem private key file in the classpath.
   * @param password optional password for the private key.
   */
  void pemFromClasspath(String certificateFile, String privateKeyFile, String password);

  /**
   * Load pem formatted identity data from a given input stream.
   *
   * @param certificateInputStream input stream to the certificate chain PEM file.
   * @param privateKeyInputStream input stream to the private key PEM file.
   */
  default void pemFromInputStream(InputStream certificateInputStream, InputStream privateKeyInputStream) {
    pemFromInputStream(certificateInputStream, privateKeyInputStream, null);
  }

  /**
   * Load pem formatted identity data from a given input stream.
   *
   * @param certificateInputStream input stream to the certificate chain PEM file.
   * @param privateKeyInputStream input stream to the private key PEM file.
   * @param password optional password for the private key.
   */
  void pemFromInputStream(
      InputStream certificateInputStream, InputStream privateKeyInputStream, String password);

  /**
   * Load pem formatted identity data from a given path in the system.
   *
   * @param certificatePath path to the certificate chain PEM file.
   * @param privateKeyPath path to the private key PEM file.
   */
  default void pemFromPath(String certificatePath, String privateKeyPath) {
    pemFromPath(certificatePath, privateKeyPath, null);
  }

  /**
   * Load pem formatted identity data from a given path in the system.
   *
   * @param certificatePath path to the certificate chain PEM file.
   * @param privateKeyPath path to the private key PEM file.
   * @param password optional password for the private key.
   */
  void pemFromPath(String certificatePath, String privateKeyPath, String password);

  /**
   * Load pem formatted identity data from a given string.
   *
   * @param certificateString PEM encoded certificate chain.
   * @param privateKeyString PEM encoded private key.
   */
  default void pemFromString(String certificateString, String privateKeyString) {
    pemFromString(certificateString, privateKeyString, null);
  }

  /**
   * Load pem formatted identity data from a given string.
   *
   * @param certificateString PEM encoded certificate chain.
   * @param privateKeyString PEM encoded private key.
   * @param password optional password for the private key.
   */
  void pemFromString(String certificateString, String privateKeyString, String password);

  /**
   * Sets a custom resource loader for loading class/module path resources using the given class.
   * This is normally used when running the application on the module path when files cannot be
   * discovered.
   *
   * @param clazz the class used to custom load resources
   * @return the updated configuration
   */
  SslConfig resourceLoader(Class<?> clazz);

  /**
   * Configure the Provider for the SSLContext.
   *
   * @param securityProvider the security provider to use.
   */
  void securityProvider(Provider securityProvider);

  /**
   * Configure the trust configuration for the server.
   *
   * @param trustConfigConsumer consumer to configure the trust configuration.
   */
  void withTrustConfig(Consumer<TrustConfig> trustConfigConsumer);
}
