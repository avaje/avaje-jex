package io.avaje.jex.jdk;

import io.avaje.jex.Jex;
import io.avaje.jex.http.ErrorCode;
import io.avaje.jex.http.HttpResponseException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionManagerTest {

  static TestPair pair = init();

  static TestPair init() {
    final Jex app = Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> {
          throw new HttpResponseException(ErrorCode.FORBIDDEN);
        })
        .post("/", ctx -> {
          throw new IllegalStateException("foo");
        })
        .get("/conflict", ctx -> {
          throw new HttpResponseException(409, "Baz");
        })
        .get("/fiveHundred", ctx -> {
          throw new IllegalArgumentException("Bar");
        })
        .exception(NullPointerException.class, (exception, ctx) -> ctx.text("npe"))
        .exception(IllegalStateException.class, (exception, ctx) -> ctx.status(222).text("Handled IllegalStateException|" + exception.getMessage())));

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get() {
    HttpResponse<String> res = pair.request().GET().asString();
    assertThat(res.statusCode()).isEqualTo(403);
    assertThat(res.body()).isEqualTo("Forbidden");
  }

  @Test
  void post() {
    HttpResponse<String> res = pair.request().body("simple").POST().asString();
    assertThat(res.statusCode()).isEqualTo(222);
    assertThat(res.body()).isEqualTo("Handled IllegalStateException|foo");
  }

  @Test
  void expect_fallback_to_default_asPlainText() {
    HttpResponse<String> res = pair.request().path("conflict").GET().asString();
    assertThat(res.statusCode()).isEqualTo(409);
    assertThat(res.body()).isEqualTo("Baz");
    assertThat(res.headers().firstValue("Content-Type").get()).contains("text/plain");
  }

  @Test
  void expect_fallback_to_default_asJson() {
    HttpResponse<String> res = pair.request().path("conflict").header("Accept", "application/json").GET().asString();
    assertThat(res.statusCode()).isEqualTo(409);
    assertThat(res.body()).isEqualTo("{\"title\": Baz, \"status\": 409}");
    assertThat(res.headers().firstValue("Content-Type").get()).contains("application/json");
  }

  @Test
  void expect_fallback_to_internalServerError() {
    HttpResponse<String> res = pair.request().path("fiveHundred").GET().asString();
    assertThat(res.statusCode()).isEqualTo(500);
    assertThat(res.body()).isEqualTo("Internal Server Error");
  }
}
