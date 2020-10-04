package io.avaje.jex.render.freemarker;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static io.avaje.jex.core.Model.toMap;
import static org.assertj.core.api.Assertions.assertThat;

class FreeMarkerServiceLoaderTest {

  static TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .routing(routing -> routing
        .get("/noModel", ctx -> ctx.render("one.ftl"))
        .get("/withModel", ctx -> ctx.render("two.ftl", toMap("message", "hello")))
      );
      // not explicitly registered so auto registered via service loader
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
