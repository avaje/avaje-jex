package io.avaje.jex.jdk;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RedirectTest {


  static TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .routing(routing -> routing
        .before("/other/*", ctx -> ctx.redirect("/two?from=filter"))
        .get("/one", ctx -> ctx.text("one"))
        .get("/two", ctx -> ctx.text("two"))
        .get("/redirect/me", ctx -> ctx.redirect("/one?from=handler"))
        .get("/other/me", ctx -> ctx.text("never hit"))
      );
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void redirect_via_handler() {
    HttpResponse<String> res = pair.request().path("redirect/me").GET().asString();
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
