package io.avaje.jex.jdk;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class MultiHandlerTest {

  static TestPair pair = init();

  static TestPair init() {
    Jex app = Jex.create()
      .routing(routing -> routing
        .get("/hi", ctx4 -> {
          if (ctx4.header("Hx-Request") != null) {
            ctx4.text("HxResponse");
          }
        })
        .get("/hi", ctx -> ctx.text("NormalResponse"))
        .get("/hi/{id}", ctx3 -> {
          if (ctx3.header("Hx-Request") != null) {
            ctx3.text("HxResponse|" + ctx3.pathParam("id"));
          }
        })
        .get("/hi/{id}", ctx2 -> {
          if (ctx2.header("H2-Request") != null) {
            ctx2.text("H2Response|" + ctx2.pathParam("id"));
          }
        })
        .get("/hi/{id}", ctx1 -> ctx1.text("NormalResponse|" + ctx1.pathParam("id")))
      );
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void test() {
    HttpResponse<String> hres = pair.request().path("hi").GET().asString();
    assertThat(hres.statusCode()).isEqualTo(200);
    assertThat(hres.body()).isEqualTo("NormalResponse");

    HttpResponse<String> hxRes = pair.request()
      .header("Hx-Request", "true")
      .path("hi")
      .GET().asString();
    assertThat(hxRes.statusCode()).isEqualTo(200);
    assertThat(hxRes.body()).isEqualTo("HxResponse");
  }

  @Test
  void testWithPathParam() {
    HttpResponse<String> hres = pair.request().path("hi/42").GET().asString();
    assertThat(hres.statusCode()).isEqualTo(200);
    assertThat(hres.body()).isEqualTo("NormalResponse|42");

    HttpResponse<String> hxRes = pair.request()
      .header("Hx-Request", "true")
      .path("hi/42")
      .GET().asString();
    assertThat(hxRes.statusCode()).isEqualTo(200);
    assertThat(hxRes.body()).isEqualTo("HxResponse|42");

    HttpResponse<String> h2Res = pair.request()
      .header("H2-Request", "true")
      .path("hi/42")
      .GET().asString();
    assertThat(h2Res.statusCode()).isEqualTo(200);
    assertThat(h2Res.body()).isEqualTo("H2Response|42");
  }
}
