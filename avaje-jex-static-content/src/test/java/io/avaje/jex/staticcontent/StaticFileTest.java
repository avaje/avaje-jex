package io.avaje.jex.staticcontent;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.test.TestPair;

class StaticFileTest {

  static TestPair pair = init();

  static TestPair init() {

    final Jex app =
        Jex.create()
            .routing(defaultCP().httpPath("/index"))
            .routing(defaultFile().httpPath("/indexFile"))
            .routing(defaultCP().httpPath("/indexWild/*"))
            .routing(defaultFile().httpPath("/indexWildFile/*"))
            .routing(defaultCP().httpPath("/sus/"))
            .routing(defaultFile().httpPath("/susFile/*"))
            .routing(StaticContentService.createCP("/logback.xml").httpPath("/single"))
            .routing(
                StaticContentService.createFile("src/test/resources/logback.xml")
                    .httpPath("/singleFile"));

    return TestPair.create(app);
  }

  private static StaticContentService defaultFile() {
    return StaticContentService.createFile("src/test/resources/public")
        .directoryIndex("index.html");
  }

  private static StaticContentService defaultCP() {
    return StaticContentService.createCP("/public").directoryIndex("index.html");
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void testGet() {
    HttpResponse<String> res = pair.request().path("index").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
  }

  @Test
  void testTraversal() {
    HttpResponse<String> res = pair.request().path("indexWild/../hmm").GET().asString();
    assertThat(res.statusCode()).isEqualTo(400);
  }

  @Test
  void getIndexWildCP() {
    HttpResponse<String> res = pair.request().path("indexWild/").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue("Content-Type").orElseThrow()).contains("html");
  }

  @Test
  void getIndex404() {
    HttpResponse<String> res = pair.request().path("index").path("index.html").GET().asString();
    assertThat(res.statusCode()).isEqualTo(404);
  }

  @Test
  void getDirContentCP() {
    HttpResponse<String> res =
        pair.request().requestTimeout(Duration.ofHours(1)).path("sus/sus.txt").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).contains("ඞ");
  }

  @Test
  void getSingleFileCP() {
    HttpResponse<String> res = pair.request().path("single").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue("Content-Type").orElseThrow()).contains("xml");
  }

  @Test
  void getIndexFile() {
    HttpResponse<String> res = pair.request().path("indexFile").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue("Content-Type").orElseThrow()).contains("html");
  }

  @Test
  void getDirContentFile() {
    HttpResponse<String> res =
        pair.request().requestTimeout(Duration.ofHours(1)).path("susFile/sus.txt").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).contains("ඞ");
  }

  @Test
  void getSingleResourceFile() {
    HttpResponse<String> res = pair.request().path("singleFile").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue("Content-Type").orElseThrow()).contains("xml");
  }

  @Test
  void getIndexWildFile() {
    HttpResponse<String> res = pair.request().path("indexWildFile/").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue("Content-Type").orElseThrow()).contains("html");
  }

  @Test
  void testFileTraversal() {
    HttpResponse<String> res = pair.request().path("indexWildFile/../traverse").GET().asString();
    assertThat(res.statusCode()).isEqualTo(400);
  }
}
