package io.avaje.jex.staticcontent;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.test.TestPair;

class CompressedStaticFileTest {

  static TestPair pair = init();

  static TestPair init() {

    final Jex app =
        Jex.create()
            .plugin(defaultCP().route("/index").build())
            .plugin(defaultFile().route("/indexFile").build())
            .plugin(defaultCP().route("/indexWild/*").build())
            .plugin(defaultFile().route("/indexWildFile/*").build())
            .plugin(defaultCP().route("/sus/").build())
            .plugin(defaultFile().route("/susFile/*").build())
            .plugin(StaticContent.createCP("/logback.xml").route("/single").build())
            .plugin(
                StaticContent.createFile("src/test/resources/logback.xml")
                    .route("/singleFile").build());

    return TestPair.create(app);
  }

  private static StaticContent.Builder defaultFile() {
    return StaticContent.createFile("src/test/resources/public")
        .directoryIndex("index.html")
        .preCompress();
  }

  private static StaticContent.Builder defaultCP() {
    return StaticContent.createCP("/public")
      .directoryIndex("index.html")
      .preCompress();
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void testGet() {
    pair.request().path("index").GET().asString();
    HttpResponse<String> res = pair.request().path("index").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
  }

  @Test
  void testTraversal() {
    pair.request().path("indexWild/../hmm").GET().asString();
    HttpResponse<String> res = pair.request().path("indexWild/../hmm").GET().asString();
    assertThat(res.statusCode()).isEqualTo(400);
  }

  @Test
  void getIndexWildCP() {
    pair.request().path("indexWild/").GET().asString();
    HttpResponse<String> res = pair.request().path("indexWild/").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue("Content-Type").orElseThrow()).contains("html");
  }

  @Test
  void getIndex404() {
    pair.request().path("index").path("index.html").GET().asString();
    HttpResponse<String> res = pair.request().path("index").path("index.html").GET().asString();
    assertThat(res.statusCode()).isEqualTo(404);
  }

  @Test
  void getDirContentCP() {
    pair.request().requestTimeout(Duration.ofHours(1)).path("sus/sus.txt").GET().asString();
    HttpResponse<String> res =
        pair.request().requestTimeout(Duration.ofHours(1)).path("sus/sus.txt").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).contains("ඞ");
  }

  @Test
  void getSingleFileCP() {
    pair.request().path("single").GET().asString();
    HttpResponse<String> res = pair.request().path("single").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue("Content-Type").orElseThrow()).contains("xml");
  }

  @Test
  void getIndexFile() {
    pair.request().path("indexFile").GET().asString();
    HttpResponse<String> res = pair.request().path("indexFile").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue("Content-Type").orElseThrow()).contains("html");
  }

  @Test
  void getDirContentFile() {
    pair.request().path("susFile/sus.txt").GET().asString();
    HttpResponse<String> res = pair.request().path("susFile/sus.txt").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).contains("ඞ");
  }

  @Test
  void getSingleResourceFile() {
    pair.request().path("singleFile").GET().asString();
    HttpResponse<String> res = pair.request().path("singleFile").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue("Content-Type").orElseThrow()).contains("xml");
  }

  @Test
  void getIndexWildFile() {
    pair.request().path("indexWildFile/").GET().asString();
    HttpResponse<String> res = pair.request().path("indexWildFile/").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue("Content-Type").orElseThrow()).contains("html");
  }

  @Test
  void testFileTraversal() {
    pair.request().path("indexWildFile/../traverse").GET().asString();
    HttpResponse<String> res = pair.request().path("indexWildFile/../traverse").GET().asString();
    assertThat(res.statusCode()).isEqualTo(400);
  }
}
