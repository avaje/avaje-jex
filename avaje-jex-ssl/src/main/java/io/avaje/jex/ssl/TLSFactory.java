package io.avaje.jex.ssl;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

final class TLSFactory {

  private static final String SSL_PROTOCOL = "TLSv1.3";
  private static final String KEY_MANAGER_ALGORITHM = "SunX509";
  private static final String TRUST_MANAGER_ALGORITHM = "PKIX";

  public static SslHttpConfigurator getSslContext(DSslConfig sslConfig) throws SslConfigException {
    try {
      SSLContext sslContext = createContext(sslConfig);

      KeyManager[] keyManagers = createKeyManagers(sslConfig);
      TrustManager[] trustManagers = createTrustManagers(sslConfig);

      sslContext.init(keyManagers, trustManagers, new SecureRandom());

      return new SslHttpConfigurator(sslContext, trustManagers != null);
    } catch (Exception e) {
      throw new SslConfigException("Failed to build SSLContext", e);
    }
  }

  private static SSLContext createContext(DSslConfig sslConfig) throws NoSuchAlgorithmException {
    if (sslConfig.securityProvider() != null) {
      return SSLContext.getInstance(SSL_PROTOCOL, sslConfig.securityProvider());
    } else {
      return SSLContext.getInstance(SSL_PROTOCOL);
    }
  }

  private static KeyManager[] createKeyManagers(DSslConfig sslConfig) throws SslConfigException {
    DSslConfig.LoadedIdentity identityType = sslConfig.loadedIdentity();

    try {
      return switch (identityType) {
        case KEY_MANAGER -> new KeyManager[] {sslConfig.keyManager()};
        case KEY_STORE -> createKeyManagersFromKeyStore(sslConfig);
        default -> throw new SslConfigException("Unsupported identity type: " + identityType);
      };
    } catch (Exception e) {
      throw new SslConfigException("Failed to create key managers", e);
    }
  }

  private static KeyManager[] createKeyManagersFromKeyStore(DSslConfig sslConfig)
      throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
    KeyManagerFactory keyManagerFactory = createKeyManagerFactory(sslConfig);
    keyManagerFactory.init(
        sslConfig.keyStore(),
        sslConfig.identityPassword() != null ? sslConfig.identityPassword().toCharArray() : null);
    return keyManagerFactory.getKeyManagers();
  }

  private static KeyManagerFactory createKeyManagerFactory(DSslConfig sslConfig)
      throws NoSuchAlgorithmException {
    if (sslConfig.securityProvider() != null) {
      return KeyManagerFactory.getInstance(KEY_MANAGER_ALGORITHM, sslConfig.securityProvider());
    } else {
      return KeyManagerFactory.getInstance(KEY_MANAGER_ALGORITHM);
    }
  }

  private static TrustManager[] createTrustManagers(DSslConfig sslConfig)
      throws SslConfigException {
    DTrustConfig trustConfig = sslConfig.trustConfig();

    if (trustConfig == null) {
      return null; // Use system default trust managers
    }

    try {
      List<KeyStore> trustStores = trustConfig.keyStores();
      List<Certificate> certificates = trustConfig.certificates();

      if (trustStores.isEmpty() && certificates.isEmpty()) {
        return null; // No custom trust configuration
      }

      KeyStore trustStore = createCombinedTrustStore(trustStores, certificates);

      TrustManagerFactory trustManagerFactory = createTrustManagerFactory(sslConfig);
      trustManagerFactory.init(trustStore);

      return trustManagerFactory.getTrustManagers();
    } catch (Exception e) {
      throw new SslConfigException("Failed to create trust managers", e);
    }
  }

  private static TrustManagerFactory createTrustManagerFactory(DSslConfig sslConfig)
      throws NoSuchAlgorithmException {
    if (sslConfig.securityProvider() != null) {
      return TrustManagerFactory.getInstance(TRUST_MANAGER_ALGORITHM, sslConfig.securityProvider());
    } else {
      return TrustManagerFactory.getInstance(TRUST_MANAGER_ALGORITHM);
    }
  }

  private static KeyStore createCombinedTrustStore(
      List<KeyStore> trustStores, List<Certificate> certificates)
      throws KeyStoreException, NoSuchAlgorithmException, CertificateException {

    KeyStore combinedTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    try {
      combinedTrustStore.load(null, null); // Initialize empty keystore
    } catch (Exception e) {
      throw new KeyStoreException("Failed to initialize trust store", e);
    }

    // Add certificates from existing trust stores
    int aliasCounter = 0;
    for (KeyStore trustStore : trustStores) {
      aliasCounter = addCertificatesFromKeyStore(combinedTrustStore, trustStore, aliasCounter);
    }

    // Add individual certificates
    for (Certificate certificate : certificates) {
      combinedTrustStore.setCertificateEntry("cert-" + aliasCounter, certificate);
      aliasCounter++;
    }

    return combinedTrustStore;
  }

  private static int addCertificatesFromKeyStore(
      KeyStore destinationStore, KeyStore sourceStore, int aliasCounter) throws KeyStoreException {

    List<String> aliases = java.util.Collections.list(sourceStore.aliases());
    for (String alias : aliases) {
      if (sourceStore.isCertificateEntry(alias)) {
        Certificate cert = sourceStore.getCertificate(alias);
        destinationStore.setCertificateEntry("imported-" + aliasCounter, cert);
        aliasCounter++;
      }
    }
    return aliasCounter;
  }
}
