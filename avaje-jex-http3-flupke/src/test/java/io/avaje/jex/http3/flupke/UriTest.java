package io.avaje.jex.http3.flupke;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;

class UriTest {

  static TestPair pair = init();

  static TestPair init() {
    var app =
        Jex.create()
            .config(c -> c.maxRequestSize(-1))
            .get("/uri/{param}", ctx -> ctx.text("uri:" + ctx.uri()))
            .get("/matchedPath/{param}", ctx -> ctx.text("matchedPath:" + ctx.matchedPath()))
            .get("/fullUrl/{param}", ctx -> ctx.text("fullUrl:" + ctx.fullUrl()))
            .get("/contextPath", ctx -> ctx.text("contextPath:" + ctx.contextPath()));
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.close();
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
        .isEqualTo("fullUrl:https://localhost:" + pair.port() + "/fullUrl/noQuery");
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
        .isEqualTo("fullUrl:https://localhost:" + pair.port() + "/fullUrl/query?a=av&b=bv");
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

}
