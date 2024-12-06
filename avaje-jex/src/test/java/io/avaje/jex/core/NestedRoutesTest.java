package io.avaje.jex.core;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class NestedRoutesTest {

  static TestPair pair = init();

  static TestPair init() {
    Jex app = Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("hello"))
        .group("api", g -> {
          g.get("/", ctx -> ctx.text("apiRoot"));
          g.get("{id}", ctx -> ctx.text("api-" + ctx.pathParam("id")));
        })
        .group("extra", g -> {
          g.get("/", ctx -> ctx.text("extraRoot"));
          g.get("{id}", ctx -> ctx.text("extra-id-" + ctx.pathParam("id")));
          g.get("more/{id}", ctx -> ctx.text("extraMore-" + ctx.pathParam("id")));
        }));
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get() {
    HttpResponse<String> res = pair.request().GET().asString();
    assertThat(res.body()).isEqualTo("hello");
  }

  @Test
  void get_api_paths() {
    var res = pair.request()
      .path("api").GET().asString();

    assertThat(res.body()).isEqualTo("apiRoot");

    res = pair.request()
      .path("api").path("99").GET().asString();

    assertThat(res.body()).isEqualTo("api-99");
  }

  @Test
  void get_extra_paths() {
    var res = pair.request()
      .path("extra").GET().asString();

    assertThat(res.body()).isEqualTo("extraRoot");

    res = pair.request()
      .path("extra").path("99").GET().asString();

    assertThat(res.body()).isEqualTo("extra-id-99");

    res = pair.request()
      .path("extra").path("more").path("42").GET().asString();

    assertThat(res.body()).isEqualTo("extraMore-42");
  }

}
