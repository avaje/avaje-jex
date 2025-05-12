package io.avaje.jex.core;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.jex.Jex;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class QueryParamTest {

  static TestPair pair = init();

  static TestPair init() {
    var app =
        Jex.create()
            .routing(
                routing ->
                    routing
                        .get("/", ctx -> ctx.text("hello"))
                        .get(
                            "/one/{id}",
                            ctx ->
                                ctx.text(
                                    "one-" + ctx.pathParam("id") + "|match:" + ctx.matchedPath()))
                        .get(
                            "/one/{id}/{b}",
                            ctx ->
                                ctx.text(
                                    "path:"
                                        + ctx.pathParamMap()
                                        + "|query:"
                                        + ctx.queryParam("z")
                                        + "|match:"
                                        + ctx.matchedPath()))
                        .get("/queryParamMap", ctx -> ctx.text("qpm: " + ctx.queryParamMap()))
                        .get("/queryParams", ctx -> ctx.text("qps: " + ctx.queryParams("a")))
                        .get("/queryString", ctx -> ctx.text("qs: " + ctx.queryString()))
                        .get(
                            "/plus/{plus}",
                            ctx -> ctx.text(ctx.pathParam("plus") + ctx.queryParam("plus")))
                        .get("/scheme", ctx -> ctx.text("scheme: " + ctx.scheme())));
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get() {
    HttpResponse<String> res = pair.request().GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("hello");
  }

  @Test
  void getOne_path() {
    var res = pair.request().path("one").path("foo").GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("one-foo|match:/one/{id}");

    res = pair.request().path("one").path("bar").GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("one-bar|match:/one/{id}");
  }

  @Test
  void getOne_path_path() {
    var res = pair.request().path("one").path("foo").path("bar").GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("path:{id=foo, b=bar}|query:null|match:/one/{id}/{b}");

    res = pair.request().path("one").path("fo").path("ba").queryParam("z", "42").GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("path:{id=fo, b=ba}|query:42|match:/one/{id}/{b}");
  }

  @Test
  void queryParamMap_when_empty() {
    HttpResponse<String> res = pair.request().path("queryParamMap").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("qpm: {}");
  }

  @Test
  void queryParamMap_keyWithMultiValues_expect_firstValueInMap() {
    HttpResponse<String> res =
        pair.request()
            .path("queryParamMap")
            .queryParam("a", "AVal0")
            .queryParam("a", "AVal1")
            .queryParam("b", "BVal")
            .GET()
            .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("qpm: {a=AVal0, b=BVal}");
  }

  @Test
  void queryParamMap_basic() {
    HttpResponse<String> res =
        pair.request()
            .path("queryParamMap")
            .queryParam("a", "AVal")
            .queryParam("b", "BVal")
            .GET()
            .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("qpm: {a=AVal, b=BVal}");
  }

  @Test
  void queryParams_basic() {
    HttpResponse<String> res =
        pair.request()
            .path("queryParams")
            .queryParam("a", "one")
            .queryParam("a", "two")
            .GET()
            .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("qps: [one, two]");
  }

  @Test
  void queryParams_when_null_expect_emptyList() {
    HttpResponse<String> res =
        pair.request().path("queryParams").queryParam("b", "one").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("qps: []");
  }

  @Test
  void queryString_when_null() {
    HttpResponse<String> res = pair.request().path("queryString").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("qs: null");
  }

  @Test
  void queryString_when_set() {
    HttpResponse<String> res =
        pair.request()
            .path("queryString")
            .queryParam("foo", "f1")
            .queryParam("bar", "b1")
            .queryParam("bar", "b2")
            .GET()
            .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("qs: foo=f1&bar=b1&bar=b2");
  }

  @Test
  void plus() {
    HttpResponse<String> res =
        pair.request().path("plus/+").queryParam("plus", "+").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("++");
  }

  @Test
  void scheme() {
    HttpResponse<String> res =
        pair.request().path("scheme").queryParam("foo", "f1").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("scheme: http");
  }
}
