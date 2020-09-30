package io.avaje.jex.server;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CharacterEncodingTest {

  static TestPair pair = init();

  static TestPair init() {
    Jex app = Jex.create()
      .routing(routing -> routing
        .get("/text", ctx -> ctx.text("суп из капусты"))
        .get("/json", ctx -> ctx.json("白菜湯"))
        .get("/html", ctx -> ctx.html("kålsuppe")));

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get() {

    var textRes = pair.request().path("text").get().asString();
    var jsonRes = pair.request().path("json").get().asString();
    var htmlRes = pair.request().path("html").get().asString();

    assertThat(contentType(textRes)).isEqualTo("text/plain;charset=utf-8");
    assertThat(contentType(jsonRes)).isEqualTo("application/json");
    assertThat(contentType(htmlRes)).isEqualTo("text/html;charset=utf-8");
    assertThat(textRes.body()).isEqualTo("суп из капусты");
    assertThat(jsonRes.body()).isEqualTo("\"白菜湯\"");
    assertThat(htmlRes.body()).isEqualTo("kålsuppe");
  }

  private String contentType(HttpResponse<String> res) {
    return res.headers().firstValue("Content-Type").get();
  }

}
