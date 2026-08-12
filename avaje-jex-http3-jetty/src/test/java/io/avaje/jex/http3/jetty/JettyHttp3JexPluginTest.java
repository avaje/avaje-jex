package io.avaje.jex.http3.jetty;

import io.avaje.http.client.HttpClient;
import io.avaje.jex.Jex;
import io.avaje.jex.test.TestPair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class JettyHttp3JexPluginTest {

  static TestPair pair = init();

  static TestPair init() {
    Jex jex = Jex.create()
      .get("/", context -> context.text("Hello, world!"))
      .plugin(JettyHttp3JexPlugin.create());
    Jex.Server server = jex.port(8080).start();

    int port = server.port();
    String protocol = jex.config().httpsConfig() != null ? "https" : "http";
    String url = protocol + "://localhost:" + port;

    HttpClient client = HttpClient.builder()
      .baseUrl(url)
      .build();

    return new TestPair(port, server, client);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void hello() {
    HttpResponse<String> response = pair.request()
      .path("/")
      .GET()
      .asString();

    assertThat(response.statusCode())
      .isEqualTo(200);
    assertThat(response.body().trim())
      .isEqualTo("Hello, world!");
  }
}
