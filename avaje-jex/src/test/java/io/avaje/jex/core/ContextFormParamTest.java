package io.avaje.jex.core;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ContextFormParamTest {

  private static final String CONTENT_TYPE = "Content-Type";
  private static final String CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";

  static TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .routing(routing -> routing
        .post("/", ctx -> ctx.text("map:" +ctx.formParamMap()))
        .post("/formParams/{key}", ctx -> ctx.text("formParams:" + ctx.formParams(ctx.pathParam("key"))))
        .post("/formParam/{key}", ctx -> ctx.text("formParam:" + ctx.formParam(ctx.pathParam("key"))))
        .post("/formParamWithDefault/{key}", ctx -> ctx.text("formParam:" + ctx.formParam(ctx.pathParam("key"), "foo")))
        .post("/reply", ctx -> ctx.text(ctx.formParam("message", "default")))
      );
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.close();
  }

  @Test
  void formParamMap() {
    HttpResponse<String> res = pair.request()
      .formParam("one", "ao")
      .formParam("one", "bo")
      .formParam("two", "z")
      .POST().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("map:{one=[ao, bo], two=[z]}");
  }


  @Test
  void formParams_one() {
    HttpResponse<String> res = pair.request()
      .formParam("one", "ao")
      .formParam("one", "bo")
      .formParam("two", "z")
      .path("formParams").path("one")
      .POST().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("formParams:[ao, bo]");
  }

  @Test
  void formParams_two() {
    HttpResponse<String> res = pair.request()
      .formParam("one", "ao")
      .formParam("one", "bo")
      .formParam("two", "z")
      .path("formParams").path("two")
      .POST().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("formParams:[z]");
  }


  @Test
  void formParam_null() {
    HttpResponse<String> res = pair.request()
      .formParam("one", "ao")
      .formParam("one", "bo")
      .formParam("two", "z")
      .path("formParam").path("doesNotExist")
      .POST().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("formParam:null");
  }

  @Test
  void formParam_first() {
    HttpResponse<String> res = pair.request()
      .formParam("one", "ao")
      .formParam("one", "bo")
      .formParam("two", "z")
      .path("formParam").path("one")
      .POST().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("formParam:ao");
  }

  @Test
  void formParam_default() {
    HttpResponse<String> res = pair.request()
      .formParam("one", "ao")
      .formParam("one", "bo")
      .formParam("two", "z")
      .path("formParamWithDefault").path("doesNotExist")
      .POST().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("formParam:foo");
  }

  @Test
  void formParam_default_first() {
    HttpResponse<String> res = pair.request()
      .formParam("one", "ao")
      .formParam("one", "bo")
      .formParam("two", "z")
      .path("formParamWithDefault").path("one")
      .POST().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("formParam:ao");
  }

  @Test
  void formParam_default_only() {
    HttpResponse<String> res = pair.request()
      .formParam("one", "ao")
      .formParam("one", "bo")
      .formParam("two", "z")
      .path("formParamWithDefault").path("two")
      .POST().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("formParam:z");
  }

  @Test
  void strictEncoding_complexMessage_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      .path("reply")
      .header("Content-Type", CONTENT_TYPE_VALUE)
      // Use "body" to bypass encoding
      .body("message=He%20asked%3A%20%22Does%201%20%2B%201%20%2F%201%20%3D%20200%25%20for%20%27efficiency%27%3F%22")
      .POST()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("He asked: \"Does 1 + 1 / 1 = 200% for 'efficiency'?\"");
  }

  @Test
  void formEncoding_complexMessage_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      .path("reply")
      .header("Content-Type", CONTENT_TYPE_VALUE)
      // Use "body" to bypass encoding
      .body("message=He+asked%3A+%22Does+1+%2B+1+%2F+1+%3D+200%25+for+%27efficiency%27%3F%22")
      .POST()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("He asked: \"Does 1 + 1 / 1 = 200% for 'efficiency'?\"");
  }

  @Test
  void strictEncoding_encodedPercent_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      .path("reply")
      .header("Content-Type", CONTENT_TYPE_VALUE)
      // Use "body" to bypass encoding
      .body("message=100%2520")
      .POST()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("100%20");
  }

  @Test
  void strictEncoding_encodedEquals_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      .path("reply")
      .header("Content-Type", CONTENT_TYPE_VALUE)
      // Use "body" to bypass encoding
      .body("message=1%2B1=2")
      .POST()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("1+1=2");
  }

  @Test
  void noSetValue_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      .path("reply")
      .header("Content-Type", CONTENT_TYPE_VALUE)
      // Use "body" to bypass encoding
      .body("message=")
      .POST()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("");
  }

  @Test
  void unboundValue_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      .path("reply")
      .header("Content-Type", CONTENT_TYPE_VALUE)
      // Use "body" to bypass encoding
      .body("=value")
      .POST()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("default");
  }

  @Test
  void strictEncoding_afterUnboundValue_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      .path("reply")
      .header("Content-Type", CONTENT_TYPE_VALUE)
      // Use "body" to bypass encoding
      .body("=value&message=1%2B1=2")
      .POST()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("1+1=2");
  }

  @Test
  void strictEncoding_beforeUnboundValue_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      .path("reply")
      .header("Content-Type", CONTENT_TYPE_VALUE)
      // Use "body" to bypass encoding
      .body("message=1%2B1=2&=value")
      .POST()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("1+1=2");
  }

  @Test
  void strictEncoding_lowerHex_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      .path("reply")
      .header("Content-Type", CONTENT_TYPE_VALUE)
      // Use "body" to bypass encoding
      .body("message=%2a")
      .POST()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("*");
  }

  @Test
  void strictEncoding_upperHex_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      .path("reply")
      .header("Content-Type", CONTENT_TYPE_VALUE)
      // Use "body" to bypass encoding
      .body("message=%2A")
      .POST()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("*");
  }

  @Test
  void strictEncoding_emoji_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      .path("reply")
      .header("Content-Type", CONTENT_TYPE_VALUE)
      // Use "body" to bypass encoding
      .body("message=%F0%9F%9A%80")
      .POST()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("\uD83D\uDE80"); // 🚀
  }

  @Test
  void strictEncoding_truncatedBytes_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      .path("reply")
      .header("Content-Type", CONTENT_TYPE_VALUE)
      // Use "body" to bypass encoding
      .body("message=%F0%9F")
      .POST()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("�");
  }

  @Test
  void strictEncoding_nullByte_roundTripsCorrectly() {
    final HttpResponse<String> res = pair.request()
      .path("reply")
      .header("Content-Type", CONTENT_TYPE_VALUE)
      // Use "body" to bypass encoding
      .body("message=config.json%00.txt")
      .POST()
      .asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("config.json\0.txt");
  }

  @Test
  void wrongContentType_rightBodyFormat_stillSends415() {
    final HttpResponse<String> res = pair.request()
      .path("reply")
      .header("Content-Type", "aaaaaaaaaaaaaaa")
      // Use "body" to bypass encoding
      .body("message=hello+world")
      .POST()
      .asString();
    assertThat(res.statusCode()).isEqualTo(415);
  }
}
