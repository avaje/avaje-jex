package io.avaje.jex.compression;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.http.HttpResponse;
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
            .get(
                "/compress",
                ctx ->
                    ctx.contentType(ContentType.APPLICATION_JSON)
                        .write(CompressionTest.class.getResourceAsStream("/64KB.json")))
            .get(
                "/flush",
                ctx -> {
                  transferStream(
                      CompressionTest.class.getResourceAsStream("/64KB.json"),
                      ctx.contentType(ContentType.APPLICATION_JSON).outputStream());
                })
            .get(
                "/sus",
                ctx -> ctx.write(CompressionTest.class.getResourceAsStream("/public/sus.txt")));

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.close();
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
  void testCompressionFlush() throws IOException {
    var res =
        pair.request()
            .header(Constants.ACCEPT_ENCODING, "deflate, gzip;q=1.0, *;q=0.5")
            .path("flush")
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
  void testCompressionRange() throws IOException {
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

  /**
   * Reads data from the input stream in chunks of 1024 bytes and writes it immediately to the
   * output stream.
   *
   * @param in The InputStream to read data from.
   * @param out The OutputStream to write data to.
   * @throws IOException If an I/O error occurs during read or write operations.
   */
  public static void transferStream(InputStream in, OutputStream out) throws IOException {
    // Define the buffer size as 1024 bytes (1KB)
    final int BUFFER_SIZE = 2048;
    byte[] buffer = new byte[BUFFER_SIZE];

    int bytesRead;
    while ((bytesRead = in.read(buffer)) != -1) {

      out.write(buffer, 0, bytesRead);
      out.flush();
    }
    out.close();
  }
}
