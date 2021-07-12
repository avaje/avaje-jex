package io.avaje.jex.jdk;

import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.HttpClientRequest;
import io.avaje.http.client.JacksonBodyAdapter;
import io.avaje.jex.Jex;

import java.time.Duration;
import java.util.Random;

/**
 * Server and Client pair for a test.
 */
public class TestPair {

  private final int port;

  private final Jex.Server server;

  private final HttpClientContext client;

  public TestPair(int port, Jex.Server server, HttpClientContext client) {
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
    return create(app, port);
  }

  public static TestPair create(Jex app, int port) {

    var jexServer = app.port(port).start();

    var url = "http://localhost:" + port;
    var client = HttpClientContext.newBuilder()
      .baseUrl(url)
      .bodyAdapter(new JacksonBodyAdapter())
      .requestTimeout(Duration.ofMinutes(2))
      .build();

    return new TestPair(port, jexServer, client);
  }
}
