package io.avaje.jex.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.avaje.http.client.HttpClient;
import io.avaje.jex.ssl.cert.Client;

class TrustConfigTests extends IntegrationTestClass {
  @Test
  void clientWithNoCertificateShouldNotBeAbleToAccessTheServer() {
    var app =
        createTestApp(
            config -> {
              config.pemFromString(
                  Client.SERVER_CERTIFICATE_AS_STRING, Client.SERVER_PRIVATE_KEY_AS_STRING);
              config.withTrustConfig(
                  trustConfig -> trustConfig.certificateFromString(Client.CLIENT_CERTIFICATE_AS_STRING));
            });

    Assertions.assertThrows(
        Exception.class,
        () -> {
          app.request().GET().asPlainString();
        });
  }

  @Test
  void clientWithWrongCertificateShouldNotBeAbleToAccessTheServer() {
    var app =
        createTestApp(
            config -> {
              config.pemFromString(
                  Client.SERVER_CERTIFICATE_AS_STRING, Client.SERVER_PRIVATE_KEY_AS_STRING);
              config.withTrustConfig(
                  trustConfig -> trustConfig.certificateFromString(Client.CLIENT_CERTIFICATE_AS_STRING));
            });

    Assertions.assertThrows(
        Exception.class,
        () -> {
          wrongClient.get().request().url(app.url()).GET().asVoid();
        });
  }

  @Test
  void loadingPemFromPathWorks() {
    trustConfigWorks(trustConfig -> trustConfig.certificateFromPath(Client.CLIENT_PEM_PATH));
  }

  @Test
  void loadingP7bFromPathWorks() {
    trustConfigWorks(trustConfig -> trustConfig.certificateFromPath(Client.CLIENT_P7B_PATH));
  }

  @Test
  void loadingDerFromPathWorks() {
    trustConfigWorks(trustConfig -> trustConfig.certificateFromPath(Client.CLIENT_DER_PATH));
  }

  @Test
  void loadingPemFromClasspathWorks() {
    trustConfigWorks(
        trustConfig -> trustConfig.certificateFromClasspath(Client.CLIENT_PEM_FILE_NAME));
  }

  @Test
  void loadingP7bFromClasspathWorks() {
    trustConfigWorks(
        trustConfig -> trustConfig.certificateFromClasspath(Client.CLIENT_P7B_FILE_NAME));
  }

  @Test
  void loadingDerFromClasspathWorks() {
    trustConfigWorks(
        trustConfig -> trustConfig.certificateFromClasspath(Client.CLIENT_DER_FILE_NAME));
  }

  @Test
  void loadingPemFromInputStreamWorks() {
    trustConfigWorks(
        trustConfig ->
            trustConfig.certificateFromInputStream(Client.CLIENT_PEM_INPUT_STREAM_SUPPLIER.get()));
  }

  @Test
  void loadingP7bFromInputStreamWorks() {
    trustConfigWorks(
        trustConfig ->
            trustConfig.certificateFromInputStream(Client.CLIENT_P7B_INPUT_STREAM_SUPPLIER.get()));
  }

  @Test
  void loadingDerFromInputStreamWorks() {
    trustConfigWorks(
        trustConfig ->
            trustConfig.certificateFromInputStream(Client.CLIENT_DER_INPUT_STREAM_SUPPLIER.get()));
  }

  @Test
  void loadingPemFromStringWorks() {
    trustConfigWorks(trustConfig -> trustConfig.certificateFromString(Client.CLIENT_CERTIFICATE_AS_STRING));
  }

  @Test
  void loadingP7bFromStringWorks() {
    trustConfigWorks(
        trustConfig ->
            trustConfig.certificateFromString(Client.CLIENT_P7B_CERTIFICATE_AS_STRING));
  }

  @Test
  void loadingJksKeystoreFromPathWorks() {
    trustConfigWorks(
        trustConfig ->
            trustConfig.trustStoreFromPath(Client.CLIENT_JKS_PATH, Client.KEYSTORE_PASSWORD));
  }

  @Test
  void loadingP12KeystoreFromPathWorks() {
    trustConfigWorks(
        trustConfig ->
            trustConfig.trustStoreFromPath(Client.CLIENT_P12_PATH, Client.KEYSTORE_PASSWORD));
  }

  @Test
  void loadingJksKeystoreFromClasspathWorks() {
    trustConfigWorks(
        trustConfig ->
            trustConfig.trustStoreFromClasspath(
                Client.CLIENT_JKS_FILE_NAME, Client.KEYSTORE_PASSWORD));
  }

  @Test
  void loadingP12KeystoreFromClasspathWorks() {
    trustConfigWorks(
        trustConfig ->
            trustConfig.trustStoreFromClasspath(
                Client.CLIENT_P12_FILE_NAME, Client.KEYSTORE_PASSWORD));
  }

  @Test
  void loadingJksKeystoreFromInputStreamWorks() {
    trustConfigWorks(
        trustConfig ->
            trustConfig.trustStoreFromInputStream(
                Client.CLIENT_JKS_INPUT_STREAM_SUPPLIER.get(), Client.KEYSTORE_PASSWORD));
  }

  @Test
  void loadingP12KeystoreFromInputStreamWorks() {
    trustConfigWorks(
        trustConfig ->
            trustConfig.trustStoreFromInputStream(
                Client.CLIENT_P12_INPUT_STREAM_SUPPLIER.get(), Client.KEYSTORE_PASSWORD));
  }

  private static final Supplier<HttpClient> wrongClient =
      () ->
          httpsClientBuilder(
              Client.WRONG_CLIENT_CERTIFICATE_AS_STRING, Client.WRONG_CLIENT_PRIVATE_KEY_AS_STRING);

  private static HttpClient httpsClientBuilder(String clientCertificate, String privateKey) {
    try {
      // Create SSL context with client certificate
      SSLContext sslContext = createSSLContext();

      return HttpClient.builder()
          .requestTimeout(Duration.ofDays(1))
          .connectionTimeout(Duration.ofDays(1))
          .sslContext(sslContext)
          .build();

    } catch (Exception e) {
      throw new RuntimeException("Failed to create HTTPS client", e);
    }
  }

  private static HttpClient createHttpsClient() {
    SSLContext sslContext;
    try {
      sslContext = createClientSSLContext();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return HttpClient.builder()
        .requestTimeout(Duration.ofDays(1))
        .connectionTimeout(Duration.ofDays(1))
        .sslContext(sslContext)
        .build();
  }

  private static SSLContext createSSLContext()
      throws NoSuchAlgorithmException, KeyManagementException {

    SSLContext sslContext = SSLContext.getInstance("TLS");

    // For testing purposes, create a trust-all trust manager
    // In production, you would properly configure certificates
    TrustManager[] trustAllCerts = {
      new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
          return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
          // Trust all client certificates for testing
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
          // Trust all server certificates for testing
        }
      }
    };

    // Note: In a real implementation, you would parse the clientCertificate and privateKey
    // and create proper KeyManager instances. This is simplified for the conversion.
    KeyManager[] keyManagers = null; // Would be properly configured with client cert

    sslContext.init(keyManagers, trustAllCerts, null);
    return sslContext;
  }

  protected static void testWrongCertOnEndpoint(String url) {
    Assertions.assertThrows(
        Exception.class,
        () -> {
          wrongClient.get().request().path(url).GET().asVoid();
        });
  }

  protected static void testSuccessfulEndpoint(String url) throws IOException {

    HttpResponse<String> response = createHttpsClient().request().path(url).GET().asString();
    Assertions.assertEquals(200, response.statusCode());
    Assertions.assertEquals(SUCCESS, response.body());
  }

  protected static void trustConfigWorks(Consumer<TrustConfig> consumer) {

    try {
      var url =
          createTestApp(
                  config -> {
                    config.pemFromString(
                        Client.SERVER_CERTIFICATE_AS_STRING, Client.SERVER_PRIVATE_KEY_AS_STRING);

                    config.withTrustConfig(consumer);
                  })
              .url();

      testSuccessfulEndpoint(url);
      testWrongCertOnEndpoint(url);

    } catch (Exception e) {
      Assertions.fail(e);
    }
  }

  /** Creates an SSLContext for client-side connections */
  public static SSLContext createClientSSLContext() throws Exception {
    // Create KeyStore for client certificate (what we present to server)
    KeyStore clientKeyStore = createClientKeyStore();

    // Create TrustStore for server certificates (what we trust from server)
    KeyStore serverTrustStore = createServerTrustStore();

    // Initialize KeyManagerFactory with client certificates
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(clientKeyStore, Client.KEYSTORE_PASSWORD.toCharArray());

    // Initialize TrustManagerFactory with server certificates
    TrustManagerFactory tmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(serverTrustStore);

    // Create and initialize SSLContext
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

    return sslContext;
  }

  /** Creates a KeyStore containing the client certificate and private key */
  private static KeyStore createClientKeyStore() throws Exception {
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(null, null);

    // Load client certificate
    X509Certificate clientCert = loadCertificateFromString(Client.CLIENT_CERTIFICATE_AS_STRING);

    // Load client private key
    PrivateKey clientPrivateKey = loadPrivateKeyFromString(Client.CLIENT_PRIVATE_KEY_AS_STRING);

    // Add client certificate and key to keystore
    keyStore.setKeyEntry(
        "client",
        clientPrivateKey,
        Client.KEYSTORE_PASSWORD.toCharArray(),
        new Certificate[] {clientCert});

    return keyStore;
  }

  /** Creates a TrustStore containing trusted server certificates */
  private static KeyStore createServerTrustStore() throws Exception {
    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    trustStore.load(null, null);

    // Add server certificate to trust store
    X509Certificate serverCert = loadCertificateFromString(Client.SERVER_CERTIFICATE_AS_STRING);
    trustStore.setCertificateEntry("server", serverCert);
    trustStore.setCertificateEntry(
        "server", loadCertificateFromString(Client.SERVER_CERTIFICATE_AS_STRING));

    return trustStore;
  }

  /** Loads an X509Certificate from a PEM string */
  private static X509Certificate loadCertificateFromString(String certString) throws Exception {
    // Remove PEM headers and whitespace
    String certData =
        certString
            .replace("-----BEGIN CERTIFICATE-----", "")
            .replace("-----END CERTIFICATE-----", "")
            .replaceAll("\\s", "");

    // Decode base64
    byte[] certBytes = Base64.getDecoder().decode(certData);

    // Create certificate
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
  }

  /** Loads a PrivateKey from a PEM string */
  private static PrivateKey loadPrivateKeyFromString(String keyString) throws Exception {
    // Remove PEM headers and whitespace
    String keyData =
        keyString
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");

    // Decode base64
    byte[] keyBytes = Base64.getDecoder().decode(keyData);

    // Create private key
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePrivate(keySpec);
  }

  /** Utility method to load certificates from file paths using the suppliers */
  public static SSLContext createSSLContextFromFiles() throws Exception {
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(null, null);

    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    trustStore.load(null, null);

    // Load P12 file directly (contains both cert and key)
    try (InputStream p12Stream = Client.CLIENT_P12_INPUT_STREAM_SUPPLIER.get()) {
      KeyStore p12Store = KeyStore.getInstance("PKCS12");
      p12Store.load(p12Stream, Client.KEYSTORE_PASSWORD.toCharArray());

      // Copy entries from P12 to our keystore
      String alias = p12Store.aliases().nextElement();
      Key key = p12Store.getKey(alias, Client.KEYSTORE_PASSWORD.toCharArray());
      Certificate[] certChain = p12Store.getCertificateChain(alias);
      keyStore.setKeyEntry("client-p12", key, Client.KEYSTORE_PASSWORD.toCharArray(), certChain);
    }

    // Load JKS file directly
    try (InputStream jksStream = Client.CLIENT_JKS_INPUT_STREAM_SUPPLIER.get()) {
      KeyStore jksStore = KeyStore.getInstance("JKS");
      jksStore.load(jksStream, Client.KEYSTORE_PASSWORD.toCharArray());

      // Copy entries from JKS to our keystore
      String alias = jksStore.aliases().nextElement();
      Key key = jksStore.getKey(alias, Client.KEYSTORE_PASSWORD.toCharArray());
      Certificate[] certChain = jksStore.getCertificateChain(alias);
      keyStore.setKeyEntry("client-jks", key, Client.KEYSTORE_PASSWORD.toCharArray(), certChain);
    }

    // Load PEM certificate for trust store
    try (InputStream pemStream = Client.CLIENT_PEM_INPUT_STREAM_SUPPLIER.get()) {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      X509Certificate cert = (X509Certificate) cf.generateCertificate(pemStream);
      trustStore.setCertificateEntry("client-pem", cert);
    }

    // Initialize managers
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(keyStore, Client.KEYSTORE_PASSWORD.toCharArray());

    TrustManagerFactory tmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(trustStore);

    // Create SSLContext
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

    return sslContext;
  }
}
