package io.avaje.jex.grizzly;

import io.avaje.jex.Context;
import io.avaje.jex.Jex;
//import io.avaje.jex.core.JsonbJsonService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

class ContextTest {

  static TestPair pair = init();

  static TestPair init() {

    var me = new ContextTest();

    final Jex app = Jex.create()
      //.configure(jex -> jex.jsonService(new JsonbJsonService()))
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("ze-get"))
        .post("/", ctx -> ctx.text("ze-post"))
        .get("/header", me::doHeader)
        .get("/headerMap", ctx -> ctx.text("req-header-map[" + ctx.headerMap() + "]"))
        .get("/host", me::doHost)
        .get("/ip", me::doIp)
        .post("/multipart", ctx -> ctx.text("isMultipart:" + ctx.isMultipart() + " isMultipartFormData:" + ctx.isMultipartFormData()))
        .get("/method", ctx -> ctx.text("method:" + ctx.method() + " path:" + ctx.path() + " protocol:" + ctx.protocol() + " port:" + ctx.port()))
        .post("/echo", ctx -> ctx.text("req-body[" + ctx.body() + "]"))
        .get("/{a}/{b}", ctx -> ctx.text("ze-get-" + ctx.pathParamMap()))
        .post("/{a}/{b}", ctx -> ctx.text("ze-post-" + ctx.pathParamMap()))
        .get("/status", me::doStatus));

    return TestPair.create(app);
  }

  private void doStatus(Context ctx) {
    ctx.status(201);
    ctx.text("status:" + ctx.status());
  }

  private void doIp(Context ctx) {
    final String ip = ctx.ip();
    requireNonNull(ip);
    ctx.text("ip:" + ip);
  }

  private void doHost(Context ctx) {
    final String host = ctx.host();
    requireNonNull(host);
    ctx.text("host:" + host);
  }

  private void doHeader(Context ctx) {
    ctx.header("From-My-Server", "Set-By-Server");
    ctx.text("req-header[" + ctx.header("From-My-Client") + "]");
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get() {
    HttpResponse<String> res = pair.request().GET().asString();
    assertThat(res.body()).isEqualTo("ze-get");
  }

  @Test
  void post() {
    HttpResponse<String> res = pair.request().body("simple").POST().asString();
    assertThat(res.body()).isEqualTo("ze-post");
  }

  @Test
  void ctx_header_getSet() {
    HttpResponse<String> res = pair.request().path("header")
      .header("From-My-Client", "client-value")
      .GET().asString();

    final Optional<String> serverSetHeader = res.headers().firstValue("From-My-Server");
    assertThat(serverSetHeader.get()).isEqualTo("Set-By-Server");
    assertThat(res.body()).isEqualTo("req-header[client-value]");
  }

  @Test
  void ctx_headerMap() {
    HttpResponse<String> res = pair.request().path("headerMap")
      .header("X-Foo", "a")
      .header("X-Bar", "b")
      .GET().asString();

    assertThat(res.body()).contains("x-foo=a"); // not maintaining case?
    assertThat(res.body()).contains("x-bar=b");
  }

  @Test
  void ctx_status() {
    HttpResponse<String> res = pair.request().path("status")
      .GET().asString();

    assertThat(res.body()).isEqualTo("status:201");
  }

  @Test
  void ctx_host() {
    HttpResponse<String> res = pair.request().path("host")
      .GET().asString();

    assertThat(res.body()).contains("host:localhost");
  }

  @Test
  void ctx_ip() {
    HttpResponse<String> res = pair.request().path("ip")
      .GET().asString();

    assertThat(res.body()).isEqualTo("ip:127.0.0.1");
  }

  @Test
  void ctx_isMultiPart_when_not() {
    HttpResponse<String> res = pair.request().path("multipart")
      .formParam("a", "aval")
      .POST().asString();

    assertThat(res.body()).isEqualTo("isMultipart:false isMultipartFormData:false");
  }


  @Test
  void ctx_isMultiPart_when_nothing() {
    HttpResponse<String> res = pair.request().path("multipart")
      .body("junk")
      .POST().asString();

    assertThat(res.body()).isEqualTo("isMultipart:false isMultipartFormData:false");
  }

//  @Test
//  void ctx_isMultiPart_when_isMultipart() {
//    HttpResponse<String> res = pair.request().path("multipart")
//      .header("Content-Type", "multipart/foo")
//      .body("junk")
//      .POST().asString();
//
//    assertThat(res.body()).isEqualTo("isMultipart:true isMultipartFormData:false");
//  }
//
//  @Test
//  void ctx_isMultiPart_when_isMultipartFormData() {
//    HttpResponse<String> res = pair.request().path("multipart")
//      .header("Content-Type", "multipart/form-data")
//      .body("junk")
//      .POST().asString();
//
//    assertThat(res.body()).isEqualTo("isMultipart:true isMultipartFormData:true");
//  }

  @Test
  void ctx_methodPathPortProtocol() {
    HttpResponse<String> res = pair.request().path("method")
      .GET().asString();

    assertThat(res.body()).isEqualTo("method:GET path:/method protocol:HTTP/1.1 port:" + pair.port());
  }

  @Test
  void post_body() {
    HttpResponse<String> res = pair.request().path("echo").body("simple").POST().asString();
    assertThat(res.body()).isEqualTo("req-body[simple]");
  }

  @Test
  void get_path_path() {
    var res = pair.request()
      .path("A").path("B").GET().asString();

    assertThat(res.body()).isEqualTo("ze-get-{a=A, b=B}");

    res = pair.request()
      .path("one").path("bar").body("simple").POST().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("ze-post-{a=one, b=bar}");
  }

}
