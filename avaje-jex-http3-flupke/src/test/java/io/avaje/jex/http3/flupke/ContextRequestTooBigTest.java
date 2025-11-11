package io.avaje.jex.http3.flupke;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;

class ContextRequestTooBigTest {

  static final TestPair pair = init();

  static TestPair init() {
    final Jex app =
        Jex.create().config(c -> c.maxRequestSize(5)).post("/", ctx -> ctx.text(ctx.body()));

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.close();
  }

  @Test
  void overSized() {
    HttpResponse<String> res = pair.request().body("amogus").POST().asString();
    assertThat(res.statusCode()).isEqualTo(413);
  }
}
