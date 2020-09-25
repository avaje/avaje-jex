package io.avaje.jex.base;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

class VerbsTest {

  static TestPair pair = init();

  static TestPair init() {
    final Jex app = Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("ze-get"))
        .post("/", ctx -> ctx.text("ze-post"))
        .get("/header", ctx -> {
          ctx.header("From-My-Server", "Set-By-Server");
          ctx.text("req-header[" + ctx.header("From-My-Client") + "]");
        })
        .get("/headerMap", ctx -> ctx.text("req-header-map[" + ctx.headerMap() + "]"))
        .get("/host", ctx -> {
          final String host = ctx.host();
          requireNonNull(host);
          ctx.text("host:" + host);
        })
        .get("/ip", ctx -> {
          final String ip = ctx.ip();
          requireNonNull(ip);
          ctx.text("ip:" + ip);
        })
        .post("/echo", ctx -> ctx.text("req-body[" + ctx.body() + "]"))
        .get("/{a}/{b}", ctx -> ctx.text("ze-get-" + ctx.pathParamMap()))
        .post("/{a}/{b}", ctx -> ctx.text("ze-post-" + ctx.pathParamMap())));

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get() {
    HttpResponse<String> res = pair.request().get().asString();
    assertThat(res.body()).isEqualTo("ze-get");
  }

  @Test
  void post() {
    HttpResponse<String> res = pair.request().body("simple").post().asString();
    assertThat(res.body()).isEqualTo("ze-post");
  }

  @Test
  void ctx_header_getSet() {
    HttpResponse<String> res = pair.request().path("header")
      .header("From-My-Client", "client-value")
      .get().asString();

    final Optional<String> serverSetHeader = res.headers().firstValue("From-My-Server");
    assertThat(serverSetHeader.get()).isEqualTo("Set-By-Server");
    assertThat(res.body()).isEqualTo("req-header[client-value]");
  }

  @Test
  void ctx_headerMap() {
    HttpResponse<String> res = pair.request().path("headerMap")
      .header("X-Foo", "a")
      .header("X-Bar", "b")
      .get().asString();

    assertThat(res.body()).contains("X-Foo=a");
    assertThat(res.body()).contains("X-Bar=b");
  }

  @Test
  void ctx_host() {
    HttpResponse<String> res = pair.request().path("host")
      .get().asString();

    assertThat(res.body()).contains("host:localhost");
  }

  @Test
  void ctx_ip() {
    HttpResponse<String> res = pair.request().path("ip")
      .get().asString();

    assertThat(res.body()).isEqualTo("ip:127.0.0.1");
  }

  @Test
  void post_body() {
    HttpResponse<String> res = pair.request().path("echo").body("simple").post().asString();
    assertThat(res.body()).isEqualTo("req-body[simple]");
  }

  @Test
  void get_path_path() {
    var res = pair.request()
      .path("A").path("B").get().asString();

    assertThat(res.body()).isEqualTo("ze-get-{a=A, b=B}");

    res = pair.request()
      .path("one").path("bar").body("simple").post().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("ze-post-{a=one, b=bar}");
  }

}
