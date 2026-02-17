package io.avaje.jex.staticcontent;

import io.avaje.jex.Jex;
import io.avaje.jex.test.TestPair;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class GzipTest {

  @Test
  void normalGzip() throws IOException {
    TestPair pair =
        TestPair.create(
            Jex.create()
                .plugin(
                    StaticContent.ofClassPath("/public")
                        .route("/index")
                        .directoryIndex("index.html")
                        .build()));

    HttpResponse<InputStream> res =
        pair.request().header("Accept-Encoding", "gzip").path("index").GET().asInputStream();
    BufferedInputStream in = new BufferedInputStream(new GZIPInputStream(res.body()));
    String s = new String(in.readAllBytes());
    assertThat(res.statusCode()).isEqualTo(200);
  }

  @Test
  void precompressGzip() throws IOException {
    TestPair pair =
        TestPair.create(
            Jex.create()
                .plugin(
                    StaticContent.ofClassPath("/public")
                        .route("/index")
                        .directoryIndex("index.html")
                        .preCompress()
                        .build()));

    HttpResponse<InputStream> res =
        pair.request().header("Accept-Encoding", "gzip").path("index").GET().asInputStream();
    BufferedInputStream in = new BufferedInputStream(new GZIPInputStream(res.body()));
    // oops java.io.EOFException: Unexpected end of ZLIB input stream
    String s = new String(in.readAllBytes());
    assertThat(res.statusCode()).isEqualTo(200);
  }

  @Test
  void precompressGzipEvenWhenNotAcceptingGzip() throws IOException {
    TestPair pair =
        TestPair.create(
            Jex.create()
                .plugin(
                    StaticContent.ofClassPath("/public")
                        .route("/index")
                        .directoryIndex("index.html")
                        .preCompress()
                        .build()));

    HttpResponse<InputStream> res =
        pair.request().header("Accept-Encoding", "gzip").path("index").GET().asInputStream();
    // first call correctly gzips
    assertThat(res.headers().firstValue("Content-Encoding")).hasValue("gzip");

    res = pair.request().path("index").GET().asInputStream();
    // second call should not be gzipped
    assertThat(res.headers().firstValue("Content-Encoding")).isEmpty();
  }
}
