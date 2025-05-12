package io.avaje.jex.core;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.jex.Jex;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class ContextLengthTest {

  static TestPair pair = init();

  static TestPair init() {
    var app =
        Jex.create()
            .routing(
                routing ->
                    routing
                        .post(
                            "/",
                            ctx ->
                                ctx.text(
                                    "contentLength:"
                                        + ctx.contentLength()
                                        + " type:"
                                        + ctx.contentType()))
                        .get("/uri/{param}", ctx -> ctx.text("uri:" + ctx.uri()))
                        .get(
                            "/matchedPath/{param}",
                            ctx -> ctx.text("matchedPath:" + ctx.matchedPath()))
                        .get("/fullUrl/{param}", ctx -> ctx.text("fullUrl:" + ctx.fullUrl()))
                        .get("/contextPath", ctx -> ctx.text("contextPath:" + ctx.contextPath()))
                        .get("/userAgent", ctx -> ctx.text("userAgent:" + ctx.userAgent())));
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void when_noReqContentType() {
    HttpResponse<String> res = pair.request().body("MyBodyContent").POST().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("contentLength:13 type:null");
  }

  @Test
  void requestContentLengthAndType_notReqContentType() {
    HttpResponse<String> res =
        pair.request().formParam("a", "my-a-val").formParam("b", "my-b-val").POST().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("contentLength:21 type:application/x-www-form-urlencoded");
  }

  @Test
  void uri() {
    HttpResponse<String> res =
        pair.request().path("uri").path("uriTest").queryParam("a", "av").GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).contains("/uri/uriTest?a=av");
  }

  @Test
  void fullUrl_no_queryString() {
    HttpResponse<String> res = pair.request().path("fullUrl").path("noQuery").GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body())
        .isEqualTo("fullUrl:http://localhost:" + pair.port() + "/fullUrl/noQuery");
  }

  @Test
  void fullUrl_queryString() {
    HttpResponse<String> res =
        pair.request()
            .path("fullUrl")
            .path("query")
            .queryParam("a", "av")
            .queryParam("b", "bv")
            .GET()
            .asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body())
        .isEqualTo("fullUrl:http://localhost:" + pair.port() + "/fullUrl/query?a=av&b=bv");
  }

  @Test
  void matchedPath() {
    HttpResponse<String> res =
        pair.request()
            .path("matchedPath")
            .path("query")
            .queryParam("a", "av")
            .queryParam("b", "bv")
            .GET()
            .asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("matchedPath:/matchedPath/{param}");
  }

  @Test
  void contextPath() {
    HttpResponse<String> res =
        pair.request().path("contextPath").queryParam("a", "av").GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("contextPath:/");
  }

  @Test
  void userAgent() {
    HttpResponse<String> res =
        pair.request().path("userAgent").queryParam("a", "av").GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).contains("userAgent:Java-http-client");
  }
}
