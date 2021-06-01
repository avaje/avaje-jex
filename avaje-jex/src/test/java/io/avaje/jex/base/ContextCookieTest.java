package io.avaje.jex.base;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ContextCookieTest {

  static TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .routing(routing -> routing
        .get("/setCookie", ctx -> ctx.cookie("ck", "val").cookie("ck2", "val2"))
        .get("/readCookie/{name}", ctx -> ctx.text("readCookie:" + ctx.cookie(ctx.pathParam("name"))))
        .get("/readCookieMap", ctx -> ctx.text("cookieMap:" + ctx.cookieMap()))
        .get("/removeCookie/{name}", ctx -> ctx.removeCookie(ctx.pathParam("name")).text("ok"))
      );
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void set_read_readMap_remove_readMap_remove_readMap() {
    HttpResponse<String> res = pair.request()
      .path("setCookie").GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);

    res = pair.request().path("readCookie").path("ck").GET().asString();
    assertThat(res.body()).isEqualTo("readCookie:val");

    res = pair.request().path("readCookie").path("ck2").GET().asString();
    assertThat(res.body()).isEqualTo("readCookie:val2");

    res = pair.request().path("readCookieMap").GET().asString();
    assertThat(res.body()).isEqualTo("cookieMap:{ck=val, ck2=val2}");

    res = pair.request().path("removeCookie").path("ck").GET().asString();
    assertThat(res.body()).isEqualTo("ok");

    res = pair.request().path("readCookieMap").GET().asString();
    assertThat(res.body()).isEqualTo("cookieMap:{ck2=val2}");

    res = pair.request().path("removeCookie").path("ck2").GET().asString();
    assertThat(res.body()).isEqualTo("ok");

    res = pair.request().path("readCookieMap").GET().asString();
    assertThat(res.body()).isEqualTo("cookieMap:{}");
  }

}
