package io.avaje.jex.base;

import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.HttpClientRequest;
import io.avaje.jex.Jex;

/**
 * Server and Client pair for a test.
 */
public class TestPair {

  private final Jex.Server jexServer;

  private final HttpClientContext client;

  public TestPair(Jex.Server jexServer, HttpClientContext client) {
    this.jexServer = jexServer;
    this.client = client;
  }

  public void shutdown() {
    jexServer.shutdown();
  }

  public HttpClientRequest request() {
    return client.request();
  }
}
