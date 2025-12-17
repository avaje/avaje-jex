package io.avaje.jex.http3.flupke;

import io.avaje.http.client.HttpClient;
import io.avaje.http.client.HttpClientRequest;
import io.avaje.jex.Jex;
import io.avaje.jex.ssl.SslPlugin;
import tech.kwik.flupke.Http3Client;

/** Server and Client pair for a test. */
public class TestPair implements AutoCloseable {
  private static final SslPlugin sslPlugin =
      SslPlugin.create(
          s ->
              s.resourceLoader(TestPair.class)
                  .keystoreFromClasspath("/my-custom-keystore.p12", "password"));
  private final int port;

  private final Jex.Server server;

  private final HttpClient client;

  public TestPair(int port, Jex.Server server, HttpClient client) {
    this.port = port;
    this.server = server;
    this.client = client;
  }

  public void shutdown() {
    server.shutdown();
    client.close();
  }

  public HttpClientRequest request() {
    return client.request();
  }

  public int port() {
    return port;
  }

  public String url() {
    return client.url().build();
  }

  /** Create a Server and Client pair for a given set of tests. */
  public static TestPair create(Jex app) {

    var jexServer = app.plugin(sslPlugin).port(0).start();
    var port = jexServer.port();
    var url = "https://localhost:" + port;
    var client =
        HttpClient.builder()
            .baseUrl(url)
            .client(
                Http3Client.newBuilder()
                    .disableCertificateCheck()
                    .sslContext(sslPlugin.sslConfigurator().getSSLContext())
                    .build())
            .build();

    return new TestPair(port, jexServer, client);
  }

  @Override
  public void close() {
    shutdown();
  }
}
