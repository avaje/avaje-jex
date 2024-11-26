package io.avaje.jex.compression;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.ResourceLocation;
import io.avaje.jex.core.HeaderKeys;
import io.avaje.jex.jdk.TestPair;

class CompressionTest {

  static TestPair pair = init();

  static TestPair init() {

    final Jex app =
        Jex.create()
            .staticResource(b -> b.httpPath("/compress").resource("/64KB.json"))
            .routing(
                r ->
                    r.get(
                        "/forced",
                        ctx -> ctx.header(HeaderKeys.CONTENT_ENCODING, "gzip").text("hi")))
            .staticResource(
                b ->
                    b.location(ResourceLocation.FILE)
                        .httpPath("/sus")
                        .resource("src/test/resources/public/sus.txt"));

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void testCompression() {
    HttpResponse<String> res =
        pair.request().header(HeaderKeys.ACCEPT_ENCODING, "gzip").path("compress").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue(HeaderKeys.CONTENT_ENCODING)).contains("gzip");
  }

  @Test
  void testNoCompression() {
    HttpResponse<String> res =
        pair.request().header(HeaderKeys.ACCEPT_ENCODING, "gzip").path("sus").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue(HeaderKeys.CONTENT_ENCODING)).isEmpty();
  }

  @Test
  void testForcedCompression() {
    HttpResponse<String> res =
        pair.request().header(HeaderKeys.ACCEPT_ENCODING, "gzip").path("forced").GET().asString();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.headers().firstValue(HeaderKeys.CONTENT_ENCODING)).contains("gzip");
  }
}
