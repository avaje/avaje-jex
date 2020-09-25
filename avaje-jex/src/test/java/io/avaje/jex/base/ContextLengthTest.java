package io.avaje.jex.base;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ContextLengthTest {

  static TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .routing(routing -> routing
        .post("/", ctx -> ctx.text("contentLength:" + ctx.contentLength() + " type:" + ctx.contentType()))
        .get("/url", ctx -> ctx.text("url:" + ctx.url()))
        .get("/fullUrl", ctx -> ctx.text("fullUrl:" + ctx.fullUrl()))
        .get("/contextPath", ctx -> ctx.text("contextPath:" + ctx.contextPath()))
        .get("/userAgent", ctx -> ctx.text("userAgent:" + ctx.userAgent()))
      );
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void when_noReqContentType() {
    HttpResponse<String> res = pair.request().body("MyBodyContent")
      .post().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("contentLength:13 type:null");
  }

  @Test
  void requestContentLengthAndType_notReqContentType() {
    HttpResponse<String> res = pair.request()
      .formParam("a", "my-a-val")
      .formParam("b", "my-b-val")
      .post().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("contentLength:21 type:application/x-www-form-urlencoded");
  }

  @Test
  void url() {
    HttpResponse<String> res = pair.request()
      .path("url")
      .param("a", "av")
      .get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("url:http://localhost:" + pair.port() + "/url");
  }

  @Test
  void fullUrl_no_queryString() {
    HttpResponse<String> res = pair.request()
      .path("fullUrl")
      .get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("fullUrl:http://localhost:" + pair.port() + "/fullUrl");
  }

  @Test
  void fullUrl_queryString() {
    HttpResponse<String> res = pair.request()
      .path("fullUrl")
      .param("a", "av")
      .param("b", "bv")
      .get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("fullUrl:http://localhost:" + pair.port() + "/fullUrl?a=av&b=bv");
  }

  @Test
  void contextPath() {
    HttpResponse<String> res = pair.request()
      .path("contextPath")
      .param("a", "av")
      .get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("contextPath:");
  }

  @Test
  void userAgent() {
    HttpResponse<String> res = pair.request()
      .path("userAgent")
      .param("a", "av")
      .get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("userAgent:Java-http-client/14.0.1");
  }
}
