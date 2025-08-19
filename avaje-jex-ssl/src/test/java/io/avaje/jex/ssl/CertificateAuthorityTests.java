package io.avaje.jex.ssl;

import java.io.IOException;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.avaje.http.client.HttpClient;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.pem.util.PemUtils;

class CertificateAuthorityTests extends IntegrationTestClass {

  @Test
  void testClientCertificateWorksWhenTrustingRootCA() {
    var keyManager = PemUtils.loadIdentityMaterial(CLIENT_FULLCHAIN_CER, CLIENT_KEY_NAME);
    var sslFactory =
        SSLFactory.builder()
            .withIdentityMaterial(keyManager)
            .withTrustingAllCertificatesWithoutValidation()
            .build();

    var client = createHttpClientWithSSL(sslFactory);
    assertClientWorks(client);
  }

  @Test
  void testClientFailsWhenNoCertificateIsProvided() {
    var sslFactory = SSLFactory.builder().withTrustingAllCertificatesWithoutValidation().build();

    var client = createHttpClientWithSSL(sslFactory);
    assertClientFails(client);
  }

  @Test
  void testClientFailsWhenCertificateWithoutChainIsProvidedAndCAIsTrusted() {
    var keyManager = PemUtils.loadIdentityMaterial(CLIENT_CER, CLIENT_KEY_NAME);
    var sslFactory =
        SSLFactory.builder()
            .withIdentityMaterial(keyManager)
            .withTrustingAllCertificatesWithoutValidation()
            .build();

    var client = createHttpClientWithSSL(sslFactory);
    assertClientFails(client);
  }

  @Test
  void testMTLSWorksWhenTrustingRootCAAndIntermediateCAIssuesBothCertificates() {
    var keyManager = PemUtils.loadIdentityMaterial(CLIENT_FULLCHAIN_CER, CLIENT_KEY_NAME);
    var sslFactory =
        SSLFactory.builder()
            .withIdentityMaterial(keyManager)
            .withTrustMaterial(PemUtils.loadTrustMaterial(ROOT_CERT_NAME))
            .withUnsafeHostnameVerifier() // we don't care about the hostname, we just want to test
            // the certificate
            .build();

    var client = createHttpClientWithSSL(sslFactory);
    assertClientWorks(client);
  }

  // Constants
  private static final String ROOT_CERT_NAME = "test-certs/ca/root-ca.cer";
  private static final String CLIENT_FULLCHAIN_CER = "test-certs/ca/client-fullchain.cer";
  private static final String CLIENT_CER = "test-certs/ca/client-nochain.cer";
  private static final String CLIENT_KEY_NAME = "test-certs/ca/client.key";
  private static final String SERVER_CERT_NAME = "test-certs/ca/server.cer";
  private static final String SERVER_KEY_NAME = "test-certs/ca/server.key";

  // Helper methods
  private HttpClient createHttpClientWithSSL(SSLFactory sslFactory) {
    return HttpClient.builder()
        .baseUrl("") // Will be set per request
        .sslContext(sslFactory.getSslContext())
        .build();
  }

  protected static void testSuccessfulEndpoint(String url, HttpClient client) throws IOException {
    HttpResponse<String> response = client.request().url(url).GET().asString();

    Assertions.assertEquals(200, response.statusCode());
    Assertions.assertEquals(SUCCESS, response.body());

    // Close the client to clean up connections
    client.close();
  }

  protected static void testWrongCertOnEndpoint(String url, HttpClient client) {
    Assertions.assertThrows(
        Exception.class,
        () -> {
          try {
            client.request().url(url).GET().asString();
          } finally {
            client.close();
          }
        });
  }

  protected static void assertClientWorks(HttpClient client) {

    try {
      var url =
          createTestApp(
                  config -> {
                    config.pemFromClasspath(SERVER_CERT_NAME, SERVER_KEY_NAME);

                    config.withTrustConfig(
                        trustConfig -> {
                          trustConfig.certificateFromClasspath(ROOT_CERT_NAME);
                        });
                  })
              .url();

      testSuccessfulEndpoint(url, client);
    } catch (Exception e) {
      Assertions.fail(e);
    }
  }

  protected static void assertClientFails(HttpClient client) {

    try {
      var url =
          createTestApp(
                  config -> {
                    config.pemFromClasspath(SERVER_CERT_NAME, SERVER_KEY_NAME);

                    config.withTrustConfig(
                        trustConfig -> {
                          trustConfig.certificateFromClasspath(ROOT_CERT_NAME);
                        });
                  })
              .url();

      testWrongCertOnEndpoint(url, client);
    } catch (Exception e) {
      Assertions.fail(e);
    }
  }
}
