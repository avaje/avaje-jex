package io.avaje.jex.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.junit.jupiter.api.Assertions;

import io.avaje.http.client.HttpClient;
import io.avaje.jex.Jex;
import io.avaje.jex.ssl.cert.Server;
import io.avaje.jex.test.TestPair;

public abstract class IntegrationTestClass {

  private void assertWorks(Consumer<SslConfig> givenConfig) {
    Consumer<SslConfig> config = givenConfig;

    var app = createTestApp(config);

    final String url = app.url();

    try {
      HttpResponse<String> response = client.request().url(url).GET().asString();
      Assertions.assertEquals(200, response.statusCode());
      Assertions.assertEquals(SUCCESS, response.body());
    } catch (Exception e) {
      Assertions.fail(e);
    } finally {
      app.shutdown();
    }
  }

  protected void assertSslWorks(Consumer<SslConfig> config) {
    assertWorks(config);
  }

  static final String SUCCESS = "success";
  static final X509TrustManager[] trustAllCerts = {
    new X509TrustManager() {
      @Override
      public void checkClientTrusted(X509Certificate[] chain, String authType) {}

      @Override
      public void checkServerTrusted(X509Certificate[] chain, String authType) {}

      @Override
      public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
      }
    }
  };
  static final AtomicInteger ports = new AtomicInteger(10000);

  static final HttpClient client = createHttpsClient();
  static final HttpClient untrustedClient = untrustedHttpsClient();

  private static HttpClient createHttpsClient() {
    SSLContext sslContext;
    try {
      sslContext = SSLContext.getInstance("TLS");
      KeyStore keyStore = KeyStore.getInstance("PKCS12");
      try (InputStream is = Server.P12_KEY_STORE_INPUT_STREAM_SUPPLIER.get()) {
        keyStore.load(is, Server.KEY_STORE_PASSWORD.toCharArray());
      }

      try (InputStream is = Server.CERTIFICATE_INPUT_STREAM_SUPPLIER.get()) {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate ca = cf.generateCertificate(is);
        keyStore.setCertificateEntry("server", ca);
      }

      TrustManagerFactory tmf =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(keyStore);

      sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
    } catch (KeyStoreException
        | IOException
        | NoSuchAlgorithmException
        | CertificateException
        | KeyManagementException e) {
      throw new RuntimeException(e);
    }

    return HttpClient.builder().sslContext(sslContext).build();
  }

  private static HttpClient untrustedHttpsClient() {
    return untrustedClientBuilder().build();
  }

  protected static HttpClient.Builder untrustedClientBuilder() {
    SSLContext sslContext;
    try {
      sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new SecureRandom());
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new RuntimeException(e);
    }

    return HttpClient.builder().sslContext(sslContext);
  }

  protected static TestPair createTestApp(Consumer<SslConfig> config) {
    return TestPair.create(
        Jex.create().get("/", ctx -> ctx.text(SUCCESS)).plugin(SslPlugin.create(config)));
  }

  protected static void testSuccessfulEndpoint(HttpClient client, String url) throws IOException {
    HttpResponse<String> response = client.request().url(url).GET().asString();
    Assertions.assertEquals(200, response.statusCode());
    Assertions.assertEquals(SUCCESS, response.body());
  }

  protected static void testSuccessfulEndpoint(String url) throws IOException {
    testSuccessfulEndpoint(client, url);
  }
}
