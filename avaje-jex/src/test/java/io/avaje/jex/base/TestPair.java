package io.avaje.jex.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.HttpClientRequest;
import io.avaje.http.client.JacksonBodyAdapter;
import io.avaje.http.client.RequestLogger;
import io.avaje.jex.Jex;

import java.util.Random;

/**
 * Server and Client pair for a test.
 */
public class TestPair {

  private static final ObjectMapper objectMapper = new ObjectMapper();

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
    var jexServer = app.port(port).start();

    var url = "http://localhost:" + port;
    var client = HttpClientContext.newBuilder()
      .withBaseUrl(url)
      .withBodyAdapter(new JacksonBodyAdapter(objectMapper))
      .withRequestListener(new RequestLogger())
      .build();

    return new TestPair(port, jexServer, client);
  }
}
