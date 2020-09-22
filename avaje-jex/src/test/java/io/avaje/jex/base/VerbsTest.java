package io.avaje.jex.base;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class VerbsTest {

  static TestPair pair = init();

  static TestPair init() {
    final Jex app = Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("ze-get"))
        .post("/", ctx -> ctx.text("ze-post"))
        .get("/{a}/{b}", ctx -> ctx.text("ze-get-" + ctx.pathParams()))
        .post("/{a}/{b}", ctx -> ctx.text("ze-post-" + ctx.pathParams())));

    return TestPair.create(app);
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
