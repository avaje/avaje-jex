package io.avaje.jex.base;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RouteSplatTest {

  static TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .routing(routing -> routing
        .get("/{id}/one", ctx -> ctx.text("id:" + ctx.pathParam("id")))
        .get("/{id}/one2", ctx -> ctx.text("id:" + ctx.pathParam("id")))
        .get("/<a>/one", ctx -> ctx.text("s1:" + ctx.pathParam("a")))
        .get("/<a>/two/<b>", ctx -> ctx.text("s2:" + ctx.pathParam("a") + "|" + ctx.pathParam("b")))
      );
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void when_utf8Encoded() {
    // This fails in Jetty 11.0.2 due to: https://github.com/eclipse/jetty.project/issues/6001
    // String path = URLEncoder.encode("java/kotlin", StandardCharsets.UTF_8)
    //  + "/two/" + URLEncoder.encode("x/y", StandardCharsets.UTF_8);
    // HttpResponse<String> res = pair.request().path(path).get().asString();

    HttpResponse<String> res = pair.request().path("java/kotlin/two/x/y").GET().asString();
    assertThat(res.body()).isEqualTo("s2:java/kotlin|x/y");
    assertThat(res.statusCode()).isEqualTo(200);
  }

  @Test
  void when_pathParamMatch() {
    HttpResponse<String> res = pair.request().path("42/one").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("id:42");
  }

  @Test
  void when_splatMatch() {
    HttpResponse<String> res = pair.request().path("42/foo/one").GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("s1:42/foo");
  }

  @Test
  void when_splats() {
    HttpResponse<String> res = pair.request().path("a/b/c/two/x/y").GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("s2:a/b/c|x/y");
  }

  @Test
  void when_noSplats() {
    HttpResponse<String> res = pair.request().path("42/one2").GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("id:42");
  }
}
