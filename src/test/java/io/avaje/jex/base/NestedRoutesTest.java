package io.avaje.jex.base;

import io.avaje.jex.Jex;
import io.avaje.jex.routes.WebApi;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static io.avaje.jex.routes.WebApi.path;
import static org.assertj.core.api.Assertions.assertThat;

class NestedRoutesTest {

  static TestPair pair = init();

  static TestPair init() {
    Jex.create()
      .get("/", ctx -> ctx.text("hello"))
      .routes(() -> {
        path("api", () -> {
          WebApi.get(ctx -> ctx.text("apiRoot"));
          WebApi.get("{id}", ctx -> ctx.text("api-" + ctx.pathParam("id")));
        });
        path("extra", () -> {
          WebApi.get(ctx -> ctx.text("extraRoot"));
          WebApi.get("{id}", ctx -> ctx.text("extra-id-" + ctx.pathParam("id")));
          WebApi.get("more/{id}", ctx -> ctx.text("extraMore-" + ctx.pathParam("id")));
        });
      });

    return HelpTest.create();
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get() {
    HttpResponse<String> res = pair.request().get().asString();
    assertThat(res.body()).isEqualTo("hello");
  }

  @Test
  void get_api_paths() {
    var res = pair.request()
      .path("api").get().asString();

    assertThat(res.body()).isEqualTo("apiRoot");

    res = pair.request()
      .path("api").path("99").get().asString();

    assertThat(res.body()).isEqualTo("api-99");
  }

  @Test
  void get_extra_paths() {
    var res = pair.request()
      .path("extra").get().asString();

    assertThat(res.body()).isEqualTo("extraRoot");

    res = pair.request()
      .path("extra").path("99").get().asString();

    assertThat(res.body()).isEqualTo("extra-id-99");

    res = pair.request()
      .path("extra").path("more").path("42").get().asString();

    assertThat(res.body()).isEqualTo("extraMore-42");
  }

}
