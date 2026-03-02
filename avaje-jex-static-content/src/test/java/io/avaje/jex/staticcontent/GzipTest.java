package io.avaje.jex.staticcontent;

import io.avaje.jex.Jex;
import io.avaje.jex.test.TestPair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class GzipTest {
  private static final TestPair pair = init();

  static TestPair init() {
    var app = Jex.create()
      .plugin(
        StaticContent.ofClassPath("/public")
          .route("/plain")
          .directoryIndex("index.html")
          .build())
      .plugin(
        StaticContent.ofClassPath("/public")
          .route("/precompress")
          .directoryIndex("index.html")
          .preCompress()
          .build())
      .plugin(
        StaticContent.ofClassPath("/public")
          .route("/precompress2")
          .directoryIndex("index.html")
          .preCompress()
          .build())
      .plugin(
        StaticContent.ofClassPath("/public")
          .route("/precompress3")
          .directoryIndex("index.html")
          .preCompress()
          .build());
    return TestPair.create(app);
  }

  @AfterAll
  static void afterAll() {
    pair.shutdown();
  }

  @Test
  void noGzip() {
    HttpResponse<InputStream> res = pair.request().path("plain").GET().asInputStream();

    assertNotGzipped(res);
  }

  @Test
  void unsupportedContentEncoding() {
    HttpResponse<InputStream> res =
      pair.request().header("Accept-Encoding", "oopla").path("plain").GET().asInputStream();

    assertNotGzipped(res);
  }

  @Test
  void normalGzip() throws IOException {
    HttpResponse<InputStream> res =
        pair.request().header("Accept-Encoding", "gzip").path("plain").GET().asInputStream();

    assertGzipped(res);
  }

  @Test
  void precompressGzip() throws IOException {
    HttpResponse<InputStream> res =
        pair.request().header("Accept-Encoding", "gzip").path("precompress").GET().asInputStream();

    assertGzipped(res);
  }

  @Test
  void precompressGzipEvenWhenNotAcceptingGzip() throws IOException {
    HttpResponse<InputStream> res =
        pair.request().header("Accept-Encoding", "gzip").path("precompress2").GET().asInputStream();
    // first call correctly gzips
    assertGzipped(res);

    res = pair.request().path("precompress2").GET().asInputStream();
    // second call should not be gzipped
    assertNotGzipped(res);
  }

  @Test
  void precompressGzipEvenWhenNotAcceptingGzipReversed() throws IOException {
    HttpResponse<InputStream> res =
      pair.request().path("precompress3").GET().asInputStream();
    assertNotGzipped(res);

    res = pair.request().path("precompress3").header("Accept-Encoding", "gzip").GET().asInputStream();
    assertGzipped(res);
  }

  private static void assertNotGzipped(HttpResponse<InputStream> res) {
    assertThat(res.headers().firstValue("Content-Encoding")).isEmpty();
    assertThat(res.headers().firstValue("Content-Length")).hasValue("4961");
    assertThat(res.headers().firstValue("Content-Type")).hasValue("text/html");
    assertThat(res.body()).hasSameContentAs(GzipTest.class.getResourceAsStream("/public/index.html"));
    assertThat(res.statusCode()).isEqualTo(200);
  }

  private static void assertGzipped(HttpResponse<InputStream> res) throws IOException {
    assertThat(res.headers().firstValue("Content-Encoding")).hasValue("gzip");
    assertThat(res.headers().firstValue("Content-Length")).hasValue("154");
    assertThat(res.headers().firstValue("Content-Type")).hasValue("text/html");
    BufferedInputStream in = new BufferedInputStream(new GZIPInputStream(res.body()));
    assertThat(in).hasSameContentAs(GzipTest.class.getResourceAsStream("/public/index.html"));
    assertThat(res.statusCode()).isEqualTo(200);
  }
}
