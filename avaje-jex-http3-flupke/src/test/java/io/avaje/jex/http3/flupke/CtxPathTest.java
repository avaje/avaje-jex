package io.avaje.jex.http3.flupke;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;

class CtxPathTest {

  static final TestPair pair = init();

  static TestPair init() {
    final Jex app = Jex.create().contextPath("/ctx/").get("", ctx -> ctx.text("ctx"));

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.close();
  }

  @Test
  void get() {
    HttpResponse<String> res = pair.request().path("ctx").GET().asString();

    assertThat(res.body()).isEqualTo("ctx");
  }

  @Test
  void getRoot404() {
    HttpResponse<String> res = pair.request().GET().asString();

    assertThat(res.statusCode()).isEqualTo(404);
  }
}
