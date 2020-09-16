package io.avaje.jex.base;

import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.HttpClientRequest;
import io.avaje.jex.JexServer;

/**
 * Server and Client pair for a test.
 */
public class TestPair {

  private final JexServer jexServer;

  private final HttpClientContext client;

  public TestPair(JexServer jexServer, HttpClientContext client) {
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
