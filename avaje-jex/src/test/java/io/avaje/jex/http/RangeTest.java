package io.avaje.jex.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.core.Constants;
import io.avaje.jex.core.TestPair;

class RangeTest {

  static final TestPair pair = init();
  private static final int chunkSize = 3000;

  static TestPair init() {
    final Jex app =
        Jex.create()
            .config(c -> c.rangeChunkSize(chunkSize))
            .get(
                "/range",
                ctx -> ctx.contentType(ContentType.VIDEO_MPEG).rangedWrite(getInput()))
            .get(
                "/range-noaudiovideo",
                ctx ->
                    ctx.contentType(ContentType.APPLICATION_OCTET_STREAM)
                        .rangedWrite(getInput()))
            .get(
                "/range-large",
                ctx -> {
                  long prefixSize = 1L << 31; // 2GB
                  long contentSize = 100L;
                  ctx.contentType(ContentType.TEXT_PLAIN)
                      .rangedWrite(
                          new LargeSeekableInput(prefixSize, contentSize),
                          prefixSize + contentSize);
                });

    return TestPair.create(app);
  }

  private static ByteArrayInputStream getInput() {

    return getInput(chunkSize);
  }

  private static ByteArrayInputStream getInput(int repeats) {
    String repeatedString =
        Stream.of("a", "b", "c").map(s -> s.repeat(repeats)).collect(Collectors.joining());
    byte[] byteArray = repeatedString.getBytes(StandardCharsets.UTF_8);

    return new ByteArrayInputStream(byteArray);
  }

  @AfterAll
  static void end() {
    pair.close();
  }

  @Test
  void downloadingWithoutRequestRange() {
    HttpResponse<String> response = pair.request().path("range-noaudiovideo").GET().asString();

    assertThat(response.body().getBytes()).isEqualTo(getInput().readAllBytes());
    assertThat(response.statusCode()).isEqualTo(200);

    assertThat(response.headers().firstValue(Constants.ACCEPT_RANGES).orElseThrow())
        .isEqualTo("bytes");
  }

  @Test
  void downloadingWithoutMaxRequestRange() {

    HttpResponse<String> response =
        pair.request()
            .path("range-noaudiovideo")
            .header(Constants.RANGE, "bytes=0-")
            .GET()
            .asString();

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValueAsLong(Constants.CONTENT_LENGTH).orElseThrow())
        .isEqualTo(getInput().available());
  }

  @Test
  void audioVideo_downloadingWithoutMaxRequestRange() {

    HttpResponse<String> response =
        pair.request().path("range").header(Constants.RANGE, "bytes=0-").GET().asString();

    assertThat(response.statusCode()).isEqualTo(206);
    assertThat(response.headers().firstValueAsLong(Constants.CONTENT_LENGTH).orElseThrow())
        .isEqualTo(getInput().available() / 3);
  }

  @Test
  void downloadResuming() {
    ByteArrayInputStream input = getInput();
    int available = input.available();

    HttpResponse<String> response =
        pair.request()
            .path("range-noaudiovideo")
            .header(Constants.RANGE, "bytes=" + chunkSize + "-")
            .GET()
            .asString();

    assertThat(response.headers().firstValue(Constants.CONTENT_RANGE).orElseThrow())
        .isEqualTo("bytes " + chunkSize + "-" + (available - 1) + "/" + available);
    assertThat(
            Integer.parseInt(response.headers().firstValue(Constants.CONTENT_LENGTH).orElseThrow()))
        .isEqualTo(available - chunkSize);

    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK_200.status());
  }

  @Test
  void testNoCompression() {
    ByteArrayInputStream input = getInput();
    int available = input.available();

    HttpResponse<String> response =
        pair.request()
            .path("range-noaudiovideo")
            .header(Constants.RANGE, "bytes=" + chunkSize + "-")
            .header(Constants.ACCEPT_ENCODING, "gzip")
            .GET()
            .asString();

    assertThat(response.headers().firstValue(Constants.CONTENT_RANGE).orElseThrow())
        .isEqualTo("bytes " + chunkSize + "-" + (available - 1) + "/" + available);
    assertThat(
            Integer.parseInt(response.headers().firstValue(Constants.CONTENT_LENGTH).orElseThrow()))
        .isEqualTo(available - chunkSize);

    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK_200.status());

  }

  @Test
  void audioVideo_specificRange() {

    HttpResponse<String> response =
        pair.request()
            .path("range")
            .header(Constants.RANGE, "bytes=" + chunkSize + "-" + (chunkSize * 2 - 1))
            .GET()
            .asString();
    assertThat(response.body()).contains("b").doesNotContain("a", "c");

    assertThat(response.statusCode()).isEqualTo(HttpStatus.PARTIAL_CONTENT_206.status());
  }

  @Test
  void audioVideo_oversizedRange() {
    HttpResponse<byte[]> response =
        pair.request()
            .path("range")
            .header(Constants.RANGE, "bytes=0-" + chunkSize * 4)
            .GET()
            .asByteArray();
    assertThat(response.body()).hasSize(chunkSize * 3);
  }

  @Test
  void audioVideo_LargeFile() {
    long prefixSize = 1L << 31; // 2GB
    long contentSize = 100L;
    HttpResponse<String> response =
        pair.request()
            .path("range-large")
            .header(Constants.RANGE, "bytes=" + prefixSize + "-" + (prefixSize + contentSize - 1))
            .GET()
            .asString();

    assertThat(response.headers().firstValue(Constants.CONTENT_RANGE).orElseThrow())
        .isEqualTo(
            "bytes "
                + prefixSize
                + "-"
                + (prefixSize + contentSize - 1)
                + "/"
                + (prefixSize + contentSize));
    assertThat(response.body()).hasSize((int) contentSize);
    assertThat(response.body()).doesNotContain(" ");
  }
}
