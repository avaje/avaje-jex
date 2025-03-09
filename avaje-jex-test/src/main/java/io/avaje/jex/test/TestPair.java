package io.avaje.jex.test;

import java.net.http.HttpClient.Version;

import io.avaje.http.client.HttpClient;
import io.avaje.http.client.HttpClientRequest;
import io.avaje.jex.Jex;

/** Server and Client pair for a test. */
public class TestPair {

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

    var jexServer = app.port(0).start();
    var port = jexServer.port();
    var url = "http://localhost:" + port;
    var client = HttpClient.builder().version(Version.HTTP_1_1).baseUrl(url).build();

    return new TestPair(port, jexServer, client);
  }
}
