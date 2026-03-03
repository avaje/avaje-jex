package io.avaje.jex.staticcontent;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.test.TestPair;

class GzipTest {
  private static final TestPair pair = init();

  static TestPair init() {
    var app =
        Jex.create()
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
                    .build())
            .plugin(
                StaticContent.ofClassPath("/public")
                    .route("/head/plain")
                    .directoryIndex("index.html")
                    .build())
            .plugin(
                StaticContent.ofClassPath("/public")
                    .route("/head/precompress")
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
  void wildcardAcceptEncoding() throws IOException {
    HttpResponse<InputStream> res =
        pair.request().header("Accept-Encoding", "*").path("plain").GET().asInputStream();

    assertGzipped(res);
  }

  @Test
  void multipleAcceptEncodings() throws IOException {
    HttpResponse<InputStream> res =
        pair.request()
            .header("Accept-Encoding", "a, b, gzip, x")
            .path("plain")
            .GET()
            .asInputStream();

    assertGzipped(res);
  }

  @Test
  void multipleAcceptEncodingsWithWeight() throws IOException {
    HttpResponse<InputStream> res =
        pair.request()
            .header("Accept-Encoding", "a;q=1, b;q=0.9, gzip;q=0.1, x;q=0")
            .path("plain")
            .GET()
            .asInputStream();

    assertGzipped(res);
  }

  @Test
  @Disabled
  // Should the 'identity' be supported
  void identityAcceptEncodingWithWeight() {
    HttpResponse<InputStream> res =
        pair.request()
            .header("Accept-Encoding", "identity;q=1, gzip;q=0")
            .path("plain")
            .GET()
            .asInputStream();

    assertNotGzipped(res);
  }

  @Test
  void unsupportedAcceptEncoding() {
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
    HttpResponse<InputStream> res = pair.request().path("precompress3").GET().asInputStream();
    assertNotGzipped(res);

    res =
        pair.request().path("precompress3").header("Accept-Encoding", "gzip").GET().asInputStream();
    assertGzipped(res);
  }

  @Test
  void headNoGzip() {
    HttpResponse<InputStream> res = pair.request().path("head/plain").HEAD().asInputStream();

    assertHeadNotGzipped(res);
  }

  @Test
  void headNormalGzip() {
    HttpResponse<InputStream> res =
        pair.request().header("Accept-Encoding", "gzip").path("head/plain").HEAD().asInputStream();

    assertHeadGzipped(res);
  }

  @Test
  void headPrecompressGzipEvenWhenNotAcceptingGzip() {
    HttpResponse<InputStream> res =
        pair.request()
            .header("Accept-Encoding", "gzip")
            .path("head/precompress")
            .HEAD()
            .asInputStream();
    assertHeadGzipped(res);

    res = pair.request().path("head/precompress").HEAD().asInputStream();
    assertHeadNotGzipped(res);
  }

  @Test
  void headPrecompressGzipEvenWhenNotAcceptingGzipReversed() {
    HttpResponse<InputStream> res = pair.request().path("head/precompress").HEAD().asInputStream();
    assertHeadNotGzipped(res);

    res =
        pair.request()
            .header("Accept-Encoding", "gzip")
            .path("head/precompress")
            .HEAD()
            .asInputStream();
    assertHeadGzipped(res);
  }

  @Test
  void headAndGetPrecompressGzipEvenWhenNotAcceptingGzip() {
    HttpResponse<InputStream> res =
        pair.request()
            .header("Accept-Encoding", "gzip")
            .path("head/precompress")
            .HEAD()
            .asInputStream();
    assertHeadGzipped(res);

    res = pair.request().path("head/precompress").GET().asInputStream();
    assertNotGzipped(res);
  }

  @Test
  void headAndGetPrecompressGzipEvenWhenNotAcceptingGzipReversed() throws IOException {
    HttpResponse<InputStream> res = pair.request().path("head/precompress").HEAD().asInputStream();
    assertHeadNotGzipped(res);

    res =
        pair.request()
            .header("Accept-Encoding", "gzip")
            .path("head/precompress")
            .GET()
            .asInputStream();
    assertGzipped(res);
  }

  @Test
  void getAndHeadPrecompressGzipEvenWhenNotAcceptingGzip() throws IOException {
    HttpResponse<InputStream> res =
        pair.request()
            .header("Accept-Encoding", "gzip")
            .path("head/precompress")
            .GET()
            .asInputStream();
    assertGzipped(res);

    res = pair.request().path("head/precompress").HEAD().asInputStream();
    assertHeadNotGzipped(res);
  }

  @Test
  void getAndHeadPrecompressGzipEvenWhenNotAcceptingGzipReversed() {
    HttpResponse<InputStream> res = pair.request().path("head/precompress").GET().asInputStream();
    assertNotGzipped(res);

    res =
        pair.request()
            .header("Accept-Encoding", "gzip")
            .path("head/precompress")
            .HEAD()
            .asInputStream();
    assertHeadGzipped(res);
  }

  private static void assertNotGzipped(HttpResponse<InputStream> res) {
    assertThat(res.body())
        .hasSameContentAs(GzipTest.class.getResourceAsStream("/public/index.html"));
  }

  private static void assertHeadNotGzipped(HttpResponse<InputStream> res) {
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().allValues("Content-Encoding")).isEmpty();

    assertThat(res.headers().allValues("Content-Length")).isEqualTo(List.of("4961"));
    assertThat(res.headers().allValues("Content-Type")).isEqualTo(List.of("text/html"));
  }

  private static void assertGzipped(HttpResponse<InputStream> res) throws IOException {
    BufferedInputStream in = new BufferedInputStream(new GZIPInputStream(res.body()));
    assertThat(in).hasSameContentAs(GzipTest.class.getResourceAsStream("/public/index.html"));
  }

  private static void assertHeadGzipped(HttpResponse<InputStream> res) {
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().allValues("Content-Encoding")).isEqualTo(List.of("gzip"));
    assertThat(res.headers().allValues("Content-Length")).is(
      new Condition<>(headers -> headers.isEmpty() || headers.equals(List.of("154")), "to be empty or contain \"154\"")
    );
    assertThat(res.headers().allValues("Content-Type")).isEqualTo(List.of("text/html"));
  }
}
