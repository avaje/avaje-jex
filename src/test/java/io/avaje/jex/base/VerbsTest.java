package io.avaje.jex.base;

import io.avaje.jex.routes.WebApi;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class VerbsTest {

  static TestPair pair = init();

  static TestPair init() {
    WebApi.get("/", ctx -> ctx.text("ze-get"));
    WebApi.post("/", ctx -> ctx.text("ze-post"));
    WebApi.get("/{a}/{b}", ctx -> ctx.text("ze-get-" + ctx.pathParams()));
    WebApi.post("/{a}/{b}", ctx -> ctx.text("ze-post-" + ctx.pathParams()));

    return HelpTest.create();
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get() {
    HttpResponse<String> res = pair.request().get().asString();
    assertThat(res.body()).isEqualTo("ze-get");
  }

  @Test
  void post() {
    HttpResponse<String> res = pair.request().body("simple").post().asString();
    assertThat(res.body()).isEqualTo("ze-post");
  }

  @Test
  void get_path_path() {
    var res = pair.request()
      .path("A").path("B").get().asString();

    assertThat(res.body()).isEqualTo("ze-get-{a=A, b=B}");

    res = pair.request()
      .path("one").path("bar").body("simple").post().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("ze-post-{a=one, b=bar}");
  }

}
