package io.avaje.jex.base;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleTest {

  static TestPair pair = init();

  static TestPair init() {
    Jex app = Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("hello"))
        .get("/one/{id}", ctx -> ctx.text("one-" + ctx.pathParam("id")))
        .get("/one/{id}/{b}", ctx -> ctx.text("path:" + ctx.pathParams() + " query:" + ctx.queryParam("z")))
      );
    return HelpTest.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get() {
    HttpResponse<String> res = pair.request().get().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("hello");
  }

  @Test
  void getOne_path() {
    var res = pair.request()
      .path("one").path("foo").get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("one-foo");

    res = pair.request()
      .path("one").path("bar").get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("one-bar");
  }

  @Test
  void getOne_path_path() {
    var res = pair.request()
      .path("one").path("foo").path("bar")
      .get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("path:{id=foo, b=bar} query:null");

    res = pair.request()
      .path("one").path("fo").path("ba").param("z", "42")
      .get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("path:{id=fo, b=ba} query:42");
  }

}
