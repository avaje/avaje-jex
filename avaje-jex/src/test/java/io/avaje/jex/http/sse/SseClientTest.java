package io.avaje.jex.http.sse;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.jex.Jex;
import io.avaje.jex.core.Constants;
import io.avaje.jex.core.TestPair;
import io.avaje.jex.core.json.JacksonJsonService;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class SseClientTest {

  static final TestPair pair = init();
  static final AtomicReference<String> afterAll = new AtomicReference<>();
  static final AtomicReference<SseClient> afterTwo = new AtomicReference<>();

  static TestPair init() {
    final var app =
        Jex.create()
            .jsonService(new JacksonJsonService())
            .sse(
                "/sse",
                sse -> {
                  for (var i = 0; i < 4; i++) {
                    sse.sendEvent("count", "hi", i + "");
                  }
                })
            .sse(
                "/is",
                sse -> {
                  for (var i = 0; i < 2; i++) {
                    sse.sendEvent(
                        "count",
                        new ByteArrayInputStream(("IS val " + 1).getBytes(StandardCharsets.UTF_8)),
                        i + "");
                  }
                })
            .sse(
                "/json",
                sse -> {
                  for (var i = 0; i < 2; i++) {
                    sse.sendEvent("count", new JsonContent(i), i + "");
                  }
                })
            .sse(
                "/keepAlive",
                sse -> {
                  Thread.startVirtualThread(
                      () -> {
                        for (var i = 0; i < 2; i++) {
                          sse.sendComment("Sent And Closed");
                          sse.close();
                        }
                      });
                  sse.keepAlive();
                })
            .sse(
                "/multi",
                sse -> {
                  sse.sendEvent("multi\nline");
                  sse.sendComment("multi\nline");
                });

    return TestPair.create(app);
  }

  public record JsonContent(int value) {}

  @AfterAll
  static void end() {
    pair.shutdown();
  }

  @Test
  void testSse() {
    final var response =
        pair.request()
            .path("sse")
            .header(Constants.ACCEPT, "text/event-stream")
            .GET()
            .asLines()
            .body()
            .toList();
    assertThat(response).hasSize(16);

    final var expected =
        """
      	id: 0
      	event: count
      	data: hi

      	id: 1
      	event: count
      	data: hi

      	id: 2
      	event: count
      	data: hi

      	id: 3
      	event: count
      	data: hi
      	""";
    assertThat(String.join("\n", response)).isEqualTo(expected);
  }

  @Test
  void testSseInputStream() {
    final var response =
        pair.request()
            .path("is")
            .header(Constants.ACCEPT, "text/event-stream")
            .GET()
            .asLines()
            .body()
            .toList();
    assertThat(response).hasSize(8);

    final var expected =
        """
  	id: 0
  	event: count
  	data: IS val 1

  	id: 1
  	event: count
  	data: IS val 1
  	""";
    assertThat(String.join("\n", response)).isEqualTo(expected);
  }

  @Test
  void testSseJson() {
    final var response =
        pair.request()
            .path("json")
            .header(Constants.ACCEPT, "text/event-stream")
            .GET()
            .asLines()
            .body()
            .toList();
    assertThat(response).hasSize(8);

    final var expected =
        """
  	id: 0
  	event: count
  	data: {"value":0}

  	id: 1
  	event: count
  	data: {"value":1}
  	""";
    assertThat(String.join("\n", response)).isEqualTo(expected);
  }

  @Test
  void testKeepAlive() {
    final var response =
        pair.request()
            .path("keepAlive")
            .header(Constants.ACCEPT, "text/event-stream")
            .GET()
            .asString()
            .body();
    assertThat(response).isEqualTo(": Sent And Closed\n");
  }

  @Test
  void testMultiLineData() {
    final var response =
        pair.request()
            .path("multi")
            .header(Constants.ACCEPT, "text/event-stream")
            .GET()
            .asString()
            .body();
    final var expected =
        """
 	event: message
 	data: multi
 	data: line

 	: multi
 	: line
 	""";
    assertThat(response).isEqualTo(expected);
  }
}
