package io.avaje.jex.base;

import io.avaje.jex.Jex;
import io.avaje.jex.http.ForbiddenResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

class ExceptionHandlerTest {

  static TestPair pair = init();

  static TestPair init() {
    final Jex app = Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> {
          throw new ForbiddenResponse();
        })
        .post("/", ctx -> {
          throw new IllegalStateException("foo");
        }))
      .exception(NullPointerException.class, (exception, ctx) -> ctx.text("npe"))
      .exception(IllegalStateException.class, (exception, ctx) ->
              ctx.status(222).text("Handled IllegalStateException|" + exception.getMessage()))
      .exception(ForbiddenResponse.class, (exception, ctx) ->
          ctx.status(223).text("Handled ForbiddenResponse|" + exception.getMessage()));

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get() {
    HttpResponse<String> res = pair.request()
      .get().asString();

//    assertThat(res.statusCode()).isEqualTo(223);
//    assertThat(res.body()).isEqualTo("Handled ForbiddenResponse");
  }

  @Test
  void post() {
    HttpResponse<String> res = pair.request().body("simple").post().asString();
//    assertThat(res.statusCode()).isEqualTo(222);
//    assertThat(res.body()).isEqualTo("ze-post");
  }


}
