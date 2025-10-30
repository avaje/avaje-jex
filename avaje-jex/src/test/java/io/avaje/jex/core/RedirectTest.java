package io.avaje.jex.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.http.NotFoundException;

class RedirectTest {

  static TestPair pair = init();

  static TestPair init() {
    var app =
        Jex.create()
            .routing(
                routing ->
                    routing
                        .filter(
                            (ctx, chain) -> {
                              if (ctx.path().contains("/other/")) ctx.redirect("/two?from=filter");
                              chain.proceed();
                            })
                        .get("/one", ctx -> ctx.text("one"))
                        .get("/two", ctx -> ctx.text("two"))
                        .get("/redirect/me", ctx -> ctx.redirect("/one?from=handler"))
                        .error(NotFoundException.class, (ctx, e) -> ctx.redirect("/one?from=error"))
                        .get("/other/me", ctx -> ctx.text("never hit")));
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.close();
  }

  @Test
  void redirect_via_handler() {
    HttpResponse<String> res = pair.request().path("redirect/me").GET().asString();
    assertThat(res.body()).isEqualTo("one");
    assertThat(res.statusCode()).isEqualTo(200);
  }

  @Test
  void redirect_via_error_handler() {
    HttpResponse<String> res = pair.request().path("redirect/error").GET().asString();
    assertThat(res.body()).isEqualTo("one");
    assertThat(res.statusCode()).isEqualTo(200);
  }

  @Test
  void redirect_via_beforeHandler() {
    HttpResponse<String> res = pair.request().path("other/me").GET().asString();
    assertThat(res.body()).isEqualTo("two");
    assertThat(res.statusCode()).isEqualTo(200);
  }
}
