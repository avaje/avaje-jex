package io.avaje.jex.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.JacksonBodyAdapter;
import io.avaje.jex.JexConfig;

import java.util.Random;

public class HelpTest {

  /**
   * Create a Server and Client pair for a given set of tests.
   */
  public static TestPair create() {

    int port = 10000 + new Random().nextInt(1000);
    var jexServer = new JexConfig()
      .port(port)
      .start();

    var url = "http://localhost:" + port;
    var client = HttpClientContext.newBuilder()
      .withBaseUrl(url)
      .withBodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
      .build();

    return new TestPair(jexServer, client);
  }
}
