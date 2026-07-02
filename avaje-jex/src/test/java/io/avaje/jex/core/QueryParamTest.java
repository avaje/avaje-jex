package io.avaje.jex.core;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class QueryParamTest {

  static TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("hello"))
        .get("/one/{id}", ctx -> ctx.text("one-" + ctx.pathParam("id") + "|match:" + ctx.matchedPath()))
        .get("/one/{id}/{b}", ctx -> ctx.text("path:" + ctx.pathParamMap() + "|query:" + ctx.queryParam("z") + "|match:" + ctx.matchedPath()))
        .get("/queryParamMap", ctx -> ctx.text("qpm: "+ctx.queryParamMap()))
        .get("/queryParams", ctx -> ctx.text("qps: "+ctx.queryParams("a")))
        .get("/queryString", ctx -> ctx.text("qs: "+ctx.queryString()))
        .get("/plus/{plus}", ctx -> ctx.text(ctx.pathParam("plus")+ctx.queryParam("plus")))
        .get("/scheme", ctx -> ctx.text("scheme: "+ctx.scheme()))
        .get("/reply", ctx -> ctx.text(ctx.queryParam("message", "default")))
        .get("/reply-all", ctx -> ctx.text(ctx.queryParams("messages").toString()))
      );
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.close();
  }

  @Test
  void get() {
    HttpResponse<String> res = pair.request().GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("hello");
  }

  @Test
  void getOne_path() {
    var res = pair.request()
      .path("one").path("foo").GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("one-foo|match:/one/{id}");

    res = pair.request()
      .path("one").path("bar").GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("one-bar|match:/one/{id}");
  }

  @Test
  void getOne_path_path() {
    var res = pair.request()
      .path("one").path("foo").path("bar")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("path:{id=foo, b=bar}|query:null|match:/one/{id}/{b}");

    res = pair.request()
      .path("one").path("fo").path("ba").queryParam("z", "42")
      .GET().asString();

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
    HttpResponse<String> res = pair.request().path("queryParamMap")
      .queryParam("a","AVal0")
      .queryParam("a","AVal1")
      .queryParam("b", "BVal")
      .GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("qpm: {a=AVal0, b=BVal}");
  }

  @Test
  void queryParamMap_basic() {
    HttpResponse<String> res = pair.request().path("queryParamMap")
      .queryParam("a","AVal")
      .queryParam("b", "BVal")
      .GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("qpm: {a=AVal, b=BVal}");
  }

  @Test
  void queryParams_basic() {
    HttpResponse<String> res = pair.request().path("queryParams")
      .queryParam("a","one")
      .queryParam("a", "two")
      .GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("qps: [one, two]");
  }

  @Test
  void queryParams_when_null_expect_emptyList() {
    HttpResponse<String> res = pair.request().path("queryParams")
      .queryParam("b","one")
      .GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("qps: []");
  }

  @Test
  void queryString_when_null() {
    HttpResponse<String> res = pair.request().path("queryString")
      .GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("qs: null");
  }

  @Test
  void queryString_when_set() {
    HttpResponse<String> res = pair.request().path("queryString")
      .queryParam("foo","f1")
      .queryParam("bar","b1")
      .queryParam("bar","b2")
      .GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("qs: foo=f1&bar=b1&bar=b2");
  }

  @Test
  void plus() {
    HttpResponse<String> res = pair.request().path("plus/+")
      .queryParam("plus","+")
      .GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("++");
  }

  @Test
  void scheme() {
    HttpResponse<String> res = pair.request().path("scheme")
      .queryParam("foo","f1")
      .GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("scheme: http");
  }

  @Test
  void usingQueryParam_complexMessage_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      // Use "path" to bypass encoding
      .path("reply")
      .queryParam("message","He asked: \"Does 1 + 1 / 1 = 200% for 'efficiency'?\"")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("He asked: \"Does 1 + 1 / 1 = 200% for 'efficiency'?\"");
  }

  @Test
  void strictEncoding_complexMessage_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      // Use "path" to bypass encoding
      .path("reply?message=He%20asked%3A%20%22Does%201%20%2B%201%20%2F%201%20%3D%20200%25%20for%20%27efficiency%27%3F%22")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("He asked: \"Does 1 + 1 / 1 = 200% for 'efficiency'?\"");
  }

  @Test
  void formEncoding_complexMessage_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      // Use "path" to bypass encoding
      .path("reply?message=He+asked%3A+%22Does+1+%2B+1+%2F+1+%3D+200%25+for+%27efficiency%27%3F%22")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("He asked: \"Does 1 + 1 / 1 = 200% for 'efficiency'?\"");
  }

  @Test
  void noParams_returnsDefault() {
    final HttpResponse<String> res = pair.request()
      .path("reply")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("default");
  }

  @Test
  void strictEncoding_encodedPercent_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      // Use "path" to bypass encoding
      .path("reply?message=100%2520")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("100%20");
  }

  @Test
  void strictEncoding_encodedEquals_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      // Use "path" to bypass encoding
      .path("reply?message=1%2B1=2")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("1+1=2");
  }

  @Test
  void noSetValue_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      // Use "path" to bypass encoding
      .path("reply?message=")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("");
  }

  @Test
  void unboundValue_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      // Use "path" to bypass encoding
      .path("reply?=value")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("default");
  }

  @Test
  void strictEncoding_afterUnboundValue_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      // Use "path" to bypass encoding
      .path("reply?=value&message=1%2B1=2")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("1+1=2");
  }

  @Test
  void strictEncoding_beforeUnboundValue_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      // Use "path" to bypass encoding
      .path("reply?message=1%2B1=2&=value")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("1+1=2");
  }

  @Test
  void strictEncoding_lowerHex_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      // Use "path" to bypass encoding
      .path("reply?message=%2a")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("*");
  }

  @Test
  void strictEncoding_upperHex_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      // Use "path" to bypass encoding
      .path("reply?message=%2A")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("*");
  }

  @Test
  void strictEncoding_emoji_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      // Use "path" to bypass encoding
      .path("reply?message=%F0%9F%9A%80")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("\uD83D\uDE80"); // 🚀
  }

  @Test
  void strictEncoding_truncatedBytes_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      // Use "path" to bypass encoding
      .path("reply?message=%F0%9F")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("�");
  }

  @Test
  void strictEncoding_nullByte_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      // Use "path" to bypass encoding
      .path("reply?message=config.json%00.txt")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("config.json\0.txt");
  }

  @Test
  void strictEncoding_repeatedInput_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      // Use "path" to bypass encoding
      .path("reply-all?messages=this&messages=is&messages=repeated")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("[this, is, repeated]");
  }

  @Test
  void strictEncoding_lonePlus_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      // Use "path" to bypass encoding
      .path("plus/+?plus=+")
      .GET()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    // The first "+" comes from the path
    //    because it should be an unmodified RFC 3986 parse
    // The second " " comes from the query params
    //    because the query params are subject to `application/x-www-form-urlencoded`
    assertThat(res.body()).isEqualTo("+ ");
  }
}
