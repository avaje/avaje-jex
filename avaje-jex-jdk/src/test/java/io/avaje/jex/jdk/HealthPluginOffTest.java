package io.avaje.jex.jdk;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class HealthPluginOffTest {

  static TestPair pair = init();

  static TestPair init() {
    final Jex app = Jex.create()
      .configure(jex -> {
        jex.config.health = false;
      })
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("hello"))
      );

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
