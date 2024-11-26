package io.avaje.jex.jdk;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static org.assertj.core.api.Assertions.assertThat;

class FilterTest {

  static final TestPair pair = init();
  static final AtomicReference<String> afterAll = new AtomicReference<>();
  static final AtomicReference<String> afterTwo = new AtomicReference<>();

  static TestPair init() {
    final Jex app =
        Jex.create()
            .routing(
                routing ->
                    routing
                        .get("/", ctx -> ctx.text("roo"))
                        .get("/noResponse", ctx -> {})
                        .get("/one", ctx -> ctx.text("one"))
                        .get("/two", ctx -> ctx.text("two"))
                        .get("/two/{id}", ctx -> ctx.text("two-id"))
                        .before(ctx -> ctx.header("before-all", "set"))
                        .filter(
                            (ctx, chain) -> {
                              if (ctx.url().contains("/two/")) {
                                ctx.header("before-two", "set");
                              }
                              chain.proceed();
                            })
                        .after(ctx -> afterAll.set("set"))
                        .filter(
                            (ctx, chain) -> {
                              chain.proceed();
                              if (ctx.url().contains("/two/")) {
                                afterTwo.set("set");
                              }
                            })
                        .get("/dummy", ctx -> ctx.text("dummy")));

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  void clearAfter() {
    afterAll.set(null);
    afterTwo.set(null);
  }

  @Test
  void get() {
    clearAfter();
    HttpResponse<String> res = pair.request().GET().asString();
    assertHasBeforeAfterAll(res);
    assertNoBeforeAfterTwo(res);

    clearAfter();
    res = pair.request().path("one").GET().asString();
    assertHasBeforeAfterAll(res);
    assertNoBeforeAfterTwo(res);

    clearAfter();
    res = pair.request().path("two").GET().asString();
    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
    assertHasBeforeAfterAll(res);
    assertNoBeforeAfterTwo(res);
  }

  @Test
  void getNoResponse() {
    clearAfter();
    HttpResponse<String> res = pair.request().path("noResponse").GET().asString();
    assertThat(res.statusCode()).isEqualTo(204);
    assertHasBeforeAfterAll(res);
    assertNoBeforeAfterTwo(res);
  }

  @Test
  void get_two_expect_extraFilters() {
    clearAfter();
    HttpResponse<String> res = pair.request().path("two/42").GET().asString();

    final HttpHeaders headers = res.headers();
    assertHasBeforeAfterAll(res);
    assertThat(headers.firstValue("before-two")).get().isEqualTo("set");
    assertThat(afterTwo.get()).isEqualTo("set");
  }

  private void assertNoBeforeAfterTwo(HttpResponse<String> res) {
    assertThat(res.headers().firstValue("before-two")).isEmpty();
    assertThat(afterTwo.get()).isNull();
  }

  private void assertHasBeforeAfterAll(HttpResponse<String> res) {
    assertThat(res.headers().firstValue("before-all")).get().isEqualTo("set");
    assertThat(afterAll.get()).isEqualTo("set");
  }
}
