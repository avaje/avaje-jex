package io.avaje.jex.core;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.jex.Jex;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class HealthPluginOffTest {

  static TestPair pair = init();

  static TestPair init() {
    final Jex app =
        Jex.create()
            .config(config -> config.health(false))
            .routing(routing -> routing.get("/", ctx -> ctx.text("hello")));

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void hello_200() {
    HttpResponse<String> res = pair.request().GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("hello");
  }

  @Test
  void liveness_404() {
    HttpResponse<String> res = pair.request().path("health/liveness").GET().asString();
    assertThat(res.statusCode()).isEqualTo(404);
  }

  @Test
  void readiness_404() {
    HttpResponse<String> res = pair.request().path("health/readiness").GET().asString();
    assertThat(res.statusCode()).isEqualTo(404);
  }
}
