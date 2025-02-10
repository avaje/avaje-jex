package io.avaje.jex.compression;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.core.Constants;
import io.avaje.jex.core.TestPair;
import io.avaje.jex.http.ContentType;

class CompressionTest {

  static TestPair pair = init();

  static TestPair init() {

    final Jex app =
        Jex.create()
            .routing(
                r ->
                    r.get(
                            "/compress",
                            ctx ->
                                ctx.contentType(ContentType.APPLICATION_JSON)
                                    .write(CompressionTest.class.getResourceAsStream("/64KB.json")))
                        .get(
                            "/sus",
                            ctx ->
                                ctx.write(
                                    CompressionTest.class.getResourceAsStream("/public/sus.txt")))
                        .get(
                            "/forced",
                            ctx -> ctx.header(Constants.CONTENT_ENCODING, "gzip").text("hi")));

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void testCompression() throws IOException {
    var res =
        pair.request()
            .header(Constants.ACCEPT_ENCODING, "deflate, gzip;q=1.0, *;q=0.5")
            .path("compress")
            .GET()
            .asInputStream();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue(Constants.CONTENT_ENCODING)).contains("gzip");

    var expected = CompressionTest.class.getResourceAsStream("/64KB.json").readAllBytes();

    final var gzipInputStream = new GZIPInputStream(res.body());
    var decompressed = gzipInputStream.readAllBytes();
    gzipInputStream.close();
    assertThat(decompressed).isEqualTo(expected);
  }

  @Test
  void testNoCompression() {
    HttpResponse<String> res =
        pair.request().header(Constants.ACCEPT_ENCODING, "gzip").path("sus").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue(Constants.CONTENT_ENCODING)).isEmpty();
  }

  @Test
  void testForcedCompression() {
    HttpResponse<String> res =
        pair.request().header(Constants.ACCEPT_ENCODING, "gzip").path("forced").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue(Constants.CONTENT_ENCODING)).contains("gzip");
  }
}
