package io.avaje.jex.base;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RouteRegexTest {

  static TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .routing(routing -> routing
        .get("/foo/{id:[0-9]+}", ctx -> ctx.text("digit:" + ctx.pathParam("id")))
        .get("/foo/count", ctx -> ctx.text("count"))
      );
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void when_digitMatch() {
    HttpResponse<String> res = pair.request().path("foo/7").get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("digit:7");
  }

  @Test
  void when_notDigitMatch() {
    HttpResponse<String> res = pair.request().path("foo/count").get().asString();


    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("count");
  }

  @Test
  void when_noMatch() {
    HttpResponse<String> res = pair.request().path("foo/a").get().asString();

    assertThat(res.statusCode()).isEqualTo(404);
  }

}
