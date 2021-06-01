package io.avaje.jex.base;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class VerbTest {

  static TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .routing(routing -> routing
        .head(ctx -> ctx.text("head"))
        .head("/head", ctx -> ctx.text("headWithPath"))
        .get(ctx -> ctx.text("get"))
        .get("/get", ctx -> ctx.text("getWithPath"))
        .put(ctx -> ctx.text("put"))
        .put("/put", ctx -> ctx.text("putWithPath"))
        .post(ctx -> ctx.text("post"))
        .post("/post", ctx -> ctx.text("postWithPath"))
        .patch(ctx -> ctx.text("patch"))
        .patch("/patch", ctx -> ctx.text("patchWithPath"))
        .delete(ctx -> ctx.text("delete"))
        .delete("/delete", ctx -> ctx.text("deleteWithPath"))
        .trace(ctx -> ctx.text("trace"))
        .trace("/trace", ctx -> ctx.text("traceWithPath"))
        .get("/dummy", ctx -> ctx.text("dummy"))
      );
    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get() {
    HttpResponse<String> res = pair.request().get().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("get");
  }

  @Test
  void get_with_path() {
    HttpResponse<String> res = pair.request().path("get").get().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("getWithPath");
  }

  @Test
  void post() {
    HttpResponse<String> res = pair.request().body("dummy").post().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("post");
  }

  @Test
  void post_with_path() {
    HttpResponse<String> res = pair.request().path("post").body("dummy").POST().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("postWithPath");
  }

  @Test
  void put() {
    HttpResponse<String> res = pair.request().body("dummy").PUT().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("put");
  }

  @Test
  void put_with_path() {
    HttpResponse<String> res = pair.request().path("put").body("dummy").PUT().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("putWithPath");
  }

  @Test
  void delete() {
    HttpResponse<String> res = pair.request().body("dummy").DELETE().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("delete");
  }

  @Test
  void delete_with_path() {
    HttpResponse<String> res = pair.request().path("delete").body("dummy").DELETE().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("deleteWithPath");
  }

  @Test
  void head() {
    HttpResponse<String> res = pair.request().HEAD().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("");
  }

  @Test
  void head_with_path() {
    HttpResponse<String> res = pair.request().path("head").HEAD().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("");
  }

  @Test
  void patch() {
    HttpResponse<String> res = pair.request().body("dummy").PATCH().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("patch");
  }

  @Test
  void patch_with_path() {
    HttpResponse<String> res = pair.request().path("patch").body("dummy").PATCH().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("patchWithPath");
  }

  @Test
  void trace() {
    HttpResponse<String> res = pair.request().body("dummy").TRACE().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("trace");
  }

  @Test
  void trace_with_path() {
    HttpResponse<String> res = pair.request().path("trace").body("dummy").TRACE().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("traceWithPath");
  }
}
