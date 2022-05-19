package io.avaje.jex.jdk;

import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.JacksonBodyAdapter;
import io.avaje.jex.Jex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class HeadersTest {

  static final int port = new Random().nextInt(1000) + 10_000;
  static Jex.Server server;
  static HttpClientContext client;

  @BeforeAll
  static void setup() {
    server = Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> {
          final String one = ctx.header("one");
          Map<String, String> obj = new LinkedHashMap<>();
          obj.put("one", one);
          ctx.json(obj);
        })
      )
      .port(port)
      .start();

    client = HttpClientContext.builder()
      .baseUrl("http://localhost:"+port)
      .bodyAdapter(new JacksonBodyAdapter())
      .build();
  }

  @Test
  void get() {

    final HttpResponse<String> hres = client.request()
      .header("one", "hello")
      .GET().asString();

    assertThat(hres.statusCode()).isEqualTo(200);
    assertThat(hres.body()).isEqualTo("{\"one\":\"hello\"}");

    server.shutdown();
  }
}
