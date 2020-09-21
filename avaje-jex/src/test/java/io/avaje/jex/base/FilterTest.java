package io.avaje.jex.base;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class FilterTest {

  static TestPair pair = init();

  static TestPair init() {
    final Jex app = Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("roo"))
        .get("/one", ctx -> ctx.text("one"))
        .get("/two", ctx -> ctx.text("two"))
        .get("/two/{id}", ctx -> ctx.text("two-id"))
        .before(ctx -> ctx.header("before-all", "set"))
        .before("/two/*", ctx -> ctx.header("before-two", "set"))
        .after(ctx -> ctx.header("after-all", "set"))
        .after("/two/*", ctx -> ctx.header("after-two", "set"))
      );

    return HelpTest.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get() {
    HttpResponse<String> res = pair.request().get().asString();
    assertHasBeforeAfterAll(res);
    assertNoBeforeAfterTwo(res);

    res = pair.request().path("one").get().asString();
    assertHasBeforeAfterAll(res);
    assertNoBeforeAfterTwo(res);

    res = pair.request().path("two").get().asString();
    assertHasBeforeAfterAll(res);
    assertNoBeforeAfterTwo(res);
  }


  @Test
  void get_two_expect_extraFilters() {
    HttpResponse<String> res = pair.request()
      .path("two/42").get().asString();

    final HttpHeaders headers = res.headers();
    assertHasBeforeAfterAll(res);
    assertThat(headers.firstValue("before-two")).get().isEqualTo("set");
    assertThat(headers.firstValue("after-two")).get().isEqualTo("set");
  }

  private void assertNoBeforeAfterTwo(HttpResponse<String> res) {
    assertThat(res.headers().firstValue("before-two")).isEmpty();
    assertThat(res.headers().firstValue("after-two")).isEmpty();
  }

  private void assertHasBeforeAfterAll(HttpResponse<String> res) {
    assertThat(res.headers().firstValue("before-all")).get().isEqualTo("set");
    assertThat(res.headers().firstValue("after-all")).get().isEqualTo("set");
  }
}
