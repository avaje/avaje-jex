package io.avaje.jex.http3.flupke;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.http.client.HttpClient;
import io.avaje.jex.Jex;

class HeadersTest {

  static final TestPair pair = init();
  static Jex.Server server;
  static HttpClient client;


  static TestPair init() {
    var app = Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> {
          final String one = ctx.header("one");
          Map<String, String> obj = new LinkedHashMap<>();
          obj.put("one", one);
          ctx.json(obj);
        })
      );


    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.close();
  }

  @Test
  void get() {

    final HttpResponse<String> hres = pair.request().header("one", "hello").GET().asString();

    assertThat(hres.statusCode()).isEqualTo(200);
    assertThat(hres.body()).isEqualTo("{\"one\":\"hello\"}");
  }
}
