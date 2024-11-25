package io.avaje.jex;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.jex.jdk.TestPair;

class StaticFileTest {

  static TestPair pair = init();

  static TestPair init() {

    final Jex app =
        Jex.create()
            .routing(
                routing ->
                    routing
                        .staticResource("/index", StaticFileTest::defaultCP)
                        .staticResource("/indexFile", StaticFileTest::defaultFile)
                        .staticResource("/indexWild/*", StaticFileTest::defaultCP)
                        .staticResource("/indexFileWild/*", StaticFileTest::defaultFile)
                        .staticResource("/sus/*", StaticFileTest::defaultCP)
                        .staticResource("/susFile/*", StaticFileTest::defaultFile)
                        .staticResource("/single", b -> b.root("/logback.xml"))
                        .staticResource(
                            "/singleFile",
                            b ->
                                b.location(ResourceLocation.FILE)
                                    .root("src/test/resources/logback.xml")));

    return TestPair.create(app);
  }

  private static StaticFileHandlerBuilder defaultFile(StaticFileHandlerBuilder b) {
    return b.location(ResourceLocation.FILE)
        .root("src/test/resources/public")
        .directoryIndex("index.html");
  }

  private static StaticFileHandlerBuilder defaultCP(StaticFileHandlerBuilder b) {
    return b.root("/public").directoryIndex("index.html");
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
    assertThat(res.body()).contains("Amogus");
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
    assertThat(res.headers().firstValue("Content-Type").orElseThrow()).contains("text");
  }

  @Test
  void getSingleResourceFile() {
    HttpResponse<String> res = pair.request().path("singleFile").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue("Content-Type").orElseThrow()).contains("xml");
  }
}
