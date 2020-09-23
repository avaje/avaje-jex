package io.avaje.jex.base;

import io.avaje.jex.Jex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class StaticContentTest {

  static TestPair pair = init();

  static TestPair init() {
    final Jex app = Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("ze-get"))
        .get("/foo", ctx -> ctx.text("ze-post"))
      ).config( config -> {
        config.staticFiles().addClasspath("/static", "static-a");
        config.staticFiles().addExternal("/other", "test-static-files");
      });

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void get_fromClassPath() {
    HttpResponse<String> res = pair.request().path("static/hello.txt").get().asString();
    assertThat(res.body().trim()).isEqualTo("hello-from-static");
    assertThat(contentType(res.headers())).isEqualTo("text/plain");
  }

  @Test
  void get_fromClassPath_another() {
    HttpResponse<String> res = pair.request().path("static/goodbye.html").get().asString();
    assertThat(res.body().trim()).isEqualTo("<html>goodbye</html>");
    assertThat(contentType(res.headers())).isEqualTo("text/html");
  }

  @Test
  void get_fromExternalFile() {
    HttpResponse<String> res = pair.request().path("other/plain-file.txt").get().asString();
    assertThat(res.body().trim()).isEqualTo("plain-file");
    assertThat(contentType(res.headers())).isEqualTo("text/plain");
  }

  @Test
  void get_fromExternalFile2() {
    HttpResponse<String> res = pair.request().path("other/basic.html").get().asString();
    assertThat(res.body().trim()).isEqualTo("<html><body>basic</body><html>");
    assertThat(contentType(res.headers())).isEqualTo("text/html");
  }

//  @Test
//  void get_fromExternal_index() {
//    HttpResponse<String> res = pair.request().path("other/").get().asString();
//    assertThat(res.body().trim()).isEqualTo("hello-from-static");
//    assertThat(contentType(res.headers())).isEqualTo("text/plain");
//  }

  private String contentType(HttpHeaders headers) {
    return headers.firstValue("Content-Type").get();
  }

}
