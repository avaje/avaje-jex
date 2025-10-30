package io.avaje.jex.core;

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
        .get("/text", ctx -> ctx.contentType("text/plain;charset=utf-8").write("суп из капусты"))
        .get("/json", ctx -> ctx.json("白菜湯"))
        .get("/html", ctx -> ctx.html("kålsuppe")));

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.close();
  }

  @Test
  void get() {

    var textRes = pair.request().path("text").GET().asString();
    var jsonRes = pair.request().path("json").GET().asString();
    var htmlRes = pair.request().path("html").GET().asString();

    assertThat(contentType(jsonRes)).isEqualTo("application/json");
    assertThat(jsonRes.body()).isEqualTo("\"白菜湯\"");
    assertThat(contentType(htmlRes)).isEqualTo("text/html;charset=utf-8");
    assertThat(htmlRes.body()).isEqualTo("kålsuppe");
    assertThat(contentType(textRes)).isEqualTo("text/plain;charset=utf-8");
    assertThat(textRes.body()).isEqualTo("суп из капусты");
  }

  private String contentType(HttpResponse<String> res) {
    return res.headers().firstValue("Content-Type").get();
  }

}
