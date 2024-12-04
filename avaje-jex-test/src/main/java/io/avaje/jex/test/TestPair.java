package io.avaje.jex.test;

import java.util.Random;

import io.avaje.http.client.HttpClient;
import io.avaje.http.client.HttpClientRequest;
import io.avaje.jex.Jex;

/**
 * Server and Client pair for a test.
 */
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

  /**
   * Create a Server and Client pair for a given set of tests.
   */
  public static TestPair create(Jex app) {

    int port = 10000 + new Random().nextInt(1000);
    var jexServer = app.port(port).start();

    var url = "http://localhost:" + port;
    var client = HttpClient.builder()
      .baseUrl(url)
      .build();

    return new TestPair(port, jexServer, client);
  }
}
