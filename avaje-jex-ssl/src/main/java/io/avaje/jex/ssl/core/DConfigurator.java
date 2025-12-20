package io.avaje.jex.ssl.core;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;

import io.avaje.jex.ssl.SSLConfigurator;
import io.avaje.jex.ssl.SslConfigException;

final class DConfigurator extends HttpsConfigurator implements SSLConfigurator {

  private static final String SSL_PROTOCOL = "TLSv1.3";
  private static final String KEY_MANAGER_ALGORITHM = "SunX509";
  private static final String TRUST_MANAGER_ALGORITHM = "PKIX";

  private final boolean clientAuth;
  private final KeyStore keyStore;
  private final String password;

  DConfigurator(SSLContext context, DSslConfig sslConfig, boolean clientAuth) {
    super(context);
    this.keyStore = sslConfig.keyStore();
    this.password = sslConfig.identityPassword();
    this.clientAuth = clientAuth;
  }

  @Override
  public void configure(HttpsParameters params) {
    var sslParams = getSSLContext().getDefaultSSLParameters();
    sslParams.setNeedClientAuth(clientAuth);
    params.setSSLParameters(sslParams);
  }

  static DConfigurator create(DSslConfig sslConfig) throws SslConfigException {
    try {
      var sslContext = createContext(sslConfig);
      var keyManagers = createKeyManagers(sslConfig);
      var trustManagers = createTrustManagers(sslConfig);
      sslContext.init(keyManagers, trustManagers, new SecureRandom());
      return new DConfigurator(sslContext, sslConfig, trustManagers != null);
    } catch (Exception e) {
      throw new SslConfigException("Failed to build SSLContext", e);
    }
  }

  private static SSLContext createContext(DSslConfig sslConfig) throws NoSuchAlgorithmException {
    if (sslConfig.securityProvider() != null) {
      return SSLContext.getInstance(SSL_PROTOCOL, sslConfig.securityProvider());
    }
    return SSLContext.getInstance(SSL_PROTOCOL);
  }

  private static KeyManager[] createKeyManagers(DSslConfig sslConfig) throws SslConfigException {
    try {
      if (sslConfig.loadedIdentity()) {
        return createKeyManagersFromKeyStore(sslConfig);
      }
      throw new IllegalStateException("No SSL Identity provided");
    } catch (Exception e) {
      throw new SslConfigException("Failed to create key managers", e);
    }
  }

  private static KeyManager[] createKeyManagersFromKeyStore(DSslConfig sslConfig)
      throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
    var keyManagerFactory = createKeyManagerFactory(sslConfig);
    keyManagerFactory.init(
        sslConfig.keyStore(),
        sslConfig.identityPassword() != null ? sslConfig.identityPassword().toCharArray() : null);
    return keyManagerFactory.getKeyManagers();
  }

  private static KeyManagerFactory createKeyManagerFactory(DSslConfig sslConfig)
      throws NoSuchAlgorithmException {
    if (sslConfig.securityProvider() != null) {
      return KeyManagerFactory.getInstance(KEY_MANAGER_ALGORITHM, sslConfig.securityProvider());
    }
    return KeyManagerFactory.getInstance(KEY_MANAGER_ALGORITHM);
  }

  private static TrustManager[] createTrustManagers(DSslConfig sslConfig)
      throws SslConfigException {
    var trustConfig = sslConfig.trustConfig();
    if (trustConfig == null) {
      return null; // Use system default trust managers
    }

    try {
      var trustStores = trustConfig.keyStores();
      var certificates = trustConfig.certificates();
      if (trustStores.isEmpty() && certificates.isEmpty()) {
        return null; // No custom trust configuration
      }

      var trustStore = createCombinedTrustStore(trustStores, certificates);
      var trustManagerFactory = createTrustManagerFactory(sslConfig);
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
    }
    return TrustManagerFactory.getInstance(TRUST_MANAGER_ALGORITHM);
  }

  private static KeyStore createCombinedTrustStore(
      List<KeyStore> trustStores, List<Certificate> certificates) throws Exception {
    var combinedTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    combinedTrustStore.load(null, null);

    // Add certificates from existing trust stores
    var aliasCounter = 0;
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
        var cert = sourceStore.getCertificate(alias);
        destinationStore.setCertificateEntry("imported-" + aliasCounter, cert);
        aliasCounter++;
      }
    }
    return aliasCounter;
  }

  @Override
  public KeyStore keyStore() {
    return keyStore;
  }

  @Override
  public String password() {
    return password;
  }
}
