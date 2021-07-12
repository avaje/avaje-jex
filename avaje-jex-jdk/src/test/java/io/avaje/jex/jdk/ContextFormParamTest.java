package io.avaje.jex.jdk;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ContextFormParamTest {

  static TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .routing(routing -> routing
        .post("/", ctx -> ctx.text("map:" +ctx.formParamMap()))
        .post("/formParams/{key}", ctx -> ctx.text("formParams:" + ctx.formParams(ctx.pathParam("key"))))
        .post("/formParam/{key}", ctx -> ctx.text("formParam:" + ctx.formParam(ctx.pathParam("key"))))
        .post("/formParamWithDefault/{key}", ctx -> ctx.text("formParam:" + ctx.formParam(ctx.pathParam("key"), "foo")))
      );
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
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
}
