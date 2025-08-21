package io.avaje.jex.ssl;

import java.io.InputStream;

/**
 * Interface defining the contract for building a trust store configuration. Implementations of this
 * interface can be used to load certificates and key stores from various sources.
 */
public interface TrustConfig {

  /**
   * Load certificate data from the classpath. The certificate can be in PEM, P7B/PKCS#7 or DER
   * format.
   *
   * @param certificateFile The name of the certificate file in the classpath.
   * @return The updated TrustConfig instance for method chaining.
   */
  TrustConfig certificateFromClasspath(String certificateFile);

  /**
   * Load certificate data from a given input stream. The certificate can be in PEM, P7B/PKCS#7 or
   * DER format.
   *
   * @param certificateInputStream input stream to the certificate file.
   * @return The updated TrustConfig instance for method chaining.
   */
  TrustConfig certificateFromInputStream(InputStream certificateInputStream);

  /**
   * Load certificate data from a given path in the system. The certificate can be in PEM,
   * P7B/PKCS#7 or DER format.
   *
   * @param certificatePath path to the certificate file.
   * @return The updated TrustConfig instance for method chaining.
   */
  TrustConfig certificateFromPath(String certificatePath);

  /**
   * Load certificate data from a given string. The certificate can be in PEM, P7B/PKCS#7 or DER
   * format.
   *
   * @param certificateString PEM encoded certificate.
   * @return The updated TrustConfig instance for method chaining.
   */
  TrustConfig certificateFromString(String certificateString);

  /**
   * Load a trust store from the classpath.
   *
   * @param trustStoreFile The name of the trust store file in the classpath.
   * @param trustStorePassword password for the trust store.
   * @return The updated TrustConfig instance for method chaining.
   */
  TrustConfig trustStoreFromClasspath(String trustStoreFile, String trustStorePassword);

  /**
   * Load a trust store from a given input stream. The trust store can be in JKS or PKCS12 format.
   *
   * @param trustStoreInputStream input stream to the trust store file.
   * @param trustStorePassword password for the trust store.
   * @return The updated TrustConfig instance for method chaining.
   */
  TrustConfig trustStoreFromInputStream(InputStream trustStoreInputStream, String trustStorePassword);

  /**
   * Load a trust store from a given path in the system. The trust store can be in JKS or PKCS12
   * format.
   *
   * @param trustStorePath path to the trust store file.
   * @param trustStorePassword password for the trust store.
   * @return The updated TrustConfig instance for method chaining.
   */
  TrustConfig trustStoreFromPath(String trustStorePath, String trustStorePassword);
}
