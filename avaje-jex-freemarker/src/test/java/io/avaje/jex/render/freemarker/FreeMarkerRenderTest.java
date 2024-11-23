package io.avaje.jex.render.freemarker;

import io.avaje.jex.Jex;
import io.avaje.jex.Routing;
import io.avaje.jex.test.TestPair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FreeMarkerRenderTest {

  static TestPair pair = init();

  static TestPair init() {
    final List<Routing.HttpService> services = List.of(new NoModel(), new WithModel());
    var app = Jex.create()
      .routing(services)
      .register(new FreeMarkerRender(), "ftl");
    return TestPair.create(app);
  }

  static class NoModel implements Routing.HttpService {
    @Override
    public void add(Routing routing) {
      routing.get("/noModel", ctx -> ctx.render("one.ftl"));
    }
  }

  static class WithModel implements Routing.HttpService {
    @Override
    public void add(Routing routing) {
      routing.get("/withModel", ctx -> ctx.render("two.ftl", Map.of("message", "hello")));
    }
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void noModel() {
    HttpResponse<String> res = pair.request().path("noModel").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body().trim()).isEqualTo("HelloFreeMarker");
  }

  @Test
  void withModel() {
    HttpResponse<String> res = pair.request().path("withModel").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body().trim()).isEqualTo("<b>hello</b>");
  }
}
