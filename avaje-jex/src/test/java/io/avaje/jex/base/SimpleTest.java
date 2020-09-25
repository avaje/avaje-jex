package io.avaje.jex.base;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleTest {

  static TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("hello"))
        .get("/one/{id}", ctx -> ctx.text("one-" + ctx.pathParam("id") + "|match:" + ctx.matchedPath()))
        .get("/one/{id}/{b}", ctx -> ctx.text("path:" + ctx.pathParamMap() + "|query:" + ctx.queryParam("z") + "|match:" + ctx.matchedPath()))
        .get("/queryParamMap", ctx -> ctx.text("qpm: "+ctx.queryParamMap()))
      );
    return TestPair.create(app);
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
    assertThat(res.body()).isEqualTo("one-foo|match:/one/{id}");

    res = pair.request()
      .path("one").path("bar").get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("one-bar|match:/one/{id}");
  }

  @Test
  void getOne_path_path() {
    var res = pair.request()
      .path("one").path("foo").path("bar")
      .get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("path:{id=foo, b=bar}|query:null|match:/one/{id}/{b}");

    res = pair.request()
      .path("one").path("fo").path("ba").param("z", "42")
      .get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("path:{id=fo, b=ba}|query:42|match:/one/{id}/{b}");
  }

  @Test
  void queryParamMap_when_empty() {
    HttpResponse<String> res = pair.request().path("queryParamMap").get().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("qpm: {}");
  }

  @Test
  void queryParamMap_keyWithMultiValues_expect_firstValueInMap() {
    HttpResponse<String> res = pair.request().path("queryParamMap")
      .param("a","AVal0")
      .param("a","AVal1")
      .param("b", "BVal")
      .get().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("qpm: {a=AVal0, b=BVal}");
  }

  @Test
  void queryParamMap_basic() {
    HttpResponse<String> res = pair.request().path("queryParamMap")
      .param("a","AVal")
      .param("b", "BVal")
      .get().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("qpm: {a=AVal, b=BVal}");
  }

}
