package io.avaje.jex.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;

class ContextRequestTooBigTest {

  static final TestPair pair = init();

  static TestPair init() {
    final Jex app =
        Jex.create().config(c -> c.maxRequestSize(5)).post("/", ctx -> ctx.text(ctx.body()));

    return TestPair.create(app);
  }

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void noBody() {
    HttpResponse<String> res = pair.request().POST().asString();
    assertThat(res.statusCode()).isEqualTo(200);
  }

  @Test
  void overSized() {
    HttpResponse<String> res = pair.request().body("amogus").POST().asString();
    assertThat(res.statusCode()).isEqualTo(413);
  }

  @Test
  void transferEncoding() throws IOException {
    HttpURLConnection connection =
        (HttpURLConnection) URI.create(pair.url()).toURL().openConnection();

    connection.setRequestMethod("POST");
    connection.setDoOutput(true);
    connection.setChunkedStreamingMode(2);

    try (OutputStream os = connection.getOutputStream()) {
      os.write("hi".getBytes());
    }

    assertThat(connection.getResponseCode()).isEqualTo(413);
  }
}
