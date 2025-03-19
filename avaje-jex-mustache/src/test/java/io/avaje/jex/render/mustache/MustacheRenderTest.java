package io.avaje.jex.render.mustache;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.jex.Jex;
import io.avaje.jex.test.TestPair;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class MustacheRenderTest {

  static TestPair pair0 = init(true);
  static TestPair pair1 = init(false);

  static TestPair init(boolean explicit) {
    var app =
        Jex.create()
            .routing(
                routing ->
                    routing
                        .get("/noModel", ctx -> ctx.render("one.mustache"))
                        .get(
                            "/withModel",
                            ctx -> ctx.render("two.mustache", Map.of("message", "hello"))));
    if (explicit) {
      app.register(new MustacheRender(), "mustache");
    }
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair0.shutdown();
    pair1.shutdown();
  }

  @Test
  void noModel() {
    HttpResponse<String> res = pair0.request().path("noModel").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body().trim()).isEqualTo("HelloFreeMarker");
  }

  @Test
  void withModel() {
    HttpResponse<String> res = pair0.request().path("withModel").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body().trim()).isEqualTo("<b>hello</b>");
  }

  @Test
  void auto_noModel() {
    HttpResponse<String> res = pair1.request().path("noModel").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body().trim()).isEqualTo("HelloFreeMarker");
  }

  @Test
  void auto_withModel() {
    HttpResponse<String> res = pair1.request().path("withModel").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body().trim()).isEqualTo("<b>hello</b>");
  }
}
