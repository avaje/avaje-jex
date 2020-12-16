package io.avaje.jex.base;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class RouteSplatTest {

  static TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .routing(routing -> routing
        .get("/{id}/one", ctx -> ctx.text("id:" + ctx.pathParam("id")))
        .get("/{id}/one2", ctx -> ctx.text("id:" + ctx.pathParam("id") + "-" + ctx.splats() + "-" + ctx.splat(0) + "-" + ctx.splat(99)))
        .get("/*/one", ctx -> ctx.text("splat:" + ctx.splat(0)))
        .get("/*/two/*", ctx -> ctx.text("splats:" + ctx.splats()))
      );
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void when_utf8Encoded() {
    String path = URLEncoder.encode("java/kotlin", StandardCharsets.UTF_8)
      + "/two/" + URLEncoder.encode("x/y", StandardCharsets.UTF_8);

    HttpResponse<String> res = pair.request().path(path).get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("splats:[java/kotlin, x/y]");
  }

  @Test
  void when_pathParamMatch() {
    HttpResponse<String> res = pair.request().path("42/one").get().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("id:42");
  }

  @Test
  void when_splatMatch() {
    HttpResponse<String> res = pair.request().path("42/foo/one").get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("splat:42/foo");
  }

  @Test
  void when_splats() {
    HttpResponse<String> res = pair.request().path("a/b/c/two/x/y").get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("splats:[a/b/c, x/y]");
  }

  @Test
  void when_noSplats() {
    HttpResponse<String> res = pair.request().path("42/one2").get().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("id:42-[]-null-null");
  }
}
