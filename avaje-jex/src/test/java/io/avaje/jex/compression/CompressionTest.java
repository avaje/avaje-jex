package io.avaje.jex.compression;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.core.Constants;
import io.avaje.jex.core.TestPair;

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
                                ctx.write(CompressionTest.class.getResourceAsStream("/64KB.json")))
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
  void testCompression() {
    HttpResponse<String> res =
        pair.request().header(Constants.ACCEPT_ENCODING, "deflate, gzip;q=1.0, *;q=0.5").path("compress").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue(Constants.CONTENT_ENCODING)).contains("gzip");
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
