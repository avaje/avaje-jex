package io.avaje.jex.http;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.jex.Jex;
import io.avaje.jex.core.TestPair;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class TrailingSlashTest {

  static TestPair pair = init();

  static TestPair init() {
    final Jex app =
        Jex.create()
            .config(c -> c.socketBacklog(0).ignoreTrailingSlashes(false))
            .get("/slash", ctx -> {});

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get() {
    HttpResponse<String> res = pair.request().path("slash/").GET().asString();
    assertThat(res.statusCode()).isEqualTo(404);
  }

  @Test
  void getNoTrailing() {
    HttpResponse<String> res = pair.request().path("slash").GET().asString();
    assertThat(res.statusCode()).isEqualTo(204);
  }
}
