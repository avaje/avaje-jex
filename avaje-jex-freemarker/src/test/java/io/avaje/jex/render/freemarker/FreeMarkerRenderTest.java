package io.avaje.jex.render.freemarker;

import io.avaje.jex.Jex;
import io.avaje.jex.test.TestPair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FreeMarkerRenderTest {

  static TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .routing(routing -> routing
        .get("/noModel", ctx -> ctx.render("one.ftl"))
        .get("/withModel", ctx -> ctx.render("two.ftl", Map.of("message", "hello")))
      )
      .register(new FreeMarkerRender(), "ftl");
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void noModel() {
    HttpResponse<String> res = pair.request().path("noModel").get().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body().trim()).isEqualTo("HelloFreeMarker");
  }

  @Test
  void withModel() {
    HttpResponse<String> res = pair.request().path("withModel").get().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body().trim()).isEqualTo("<b>hello</b>");
  }
}
