package io.avaje.jex.http3.flupke.webtransport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.http3.flupke.FlupkeJexPlugin;

class StreamTypeTest extends WebTransportBaseTest {

  @Test
  void testBasicBidirectionalEchoAndHttp3Endpoint() throws Exception {
    var webTransport =
        FlupkeJexPlugin.create().webTransport("/echo", b -> b.onBiDirectionalStream(this::echo));

    jex =
        Jex.create()
            .plugin(ssl)
            .plugin(webTransport)
            .get(
                "/",
                ctx -> {
                  assertEquals("h3", ctx.exchange().getProtocol());
                  ctx.text("hello world");
                })
            .port(0)
            .start();

    localhost = URI.create("https://localhost:%s".formatted(jex.port()));
    // Test regular HTTP/3 endpoint
    assertEquals(
        "hello world",
        client
            .send(HttpRequest.newBuilder().uri(localhost).GET().build(), BodyHandlers.ofString())
            .body());

    // Test WebTransport echo
    var clientSessionFactory = createClientSessionFactory("/echo");
    var session = clientSessionFactory.createSession(localhost.resolve("/echo"));
    session.open();

    var bidirectionalStream = session.createBidirectionalStream();
    var message = "Hello, WebTransport!";
    bidirectionalStream.getOutputStream().write(message.getBytes());
    bidirectionalStream.getOutputStream().close();

    var response = new ByteArrayOutputStream();
    bidirectionalStream.getInputStream().transferTo(response);

    assertEquals(message, response.toString(StandardCharsets.UTF_8));

    session.close();
  }

  @Test
  void testClientInitiatedUnidirectionalStream() throws Exception {
    var latch = new CountDownLatch(1);
    var receivedMessage = new AtomicReference<String>();

    var webTransport =
        FlupkeJexPlugin.create()
            .webTransport(
                "/uni",
                b ->
                    b.onUniDirectionalStream(
                        stream -> {
                          try (stream) {
                            var baos = new ByteArrayOutputStream();
                            stream.requestStream().transferTo(baos);
                            receivedMessage.set(baos.toString(StandardCharsets.UTF_8));
                            latch.countDown();
                          } catch (IOException e) {
                            fail("Failed to read unidirectional stream: " + e.getMessage());
                          }
                        }));

    startServer(webTransport);

    var clientSessionFactory = createClientSessionFactory("/uni");
    var session = clientSessionFactory.createSession(localhost.resolve("/uni"));
    session.open();

    var uniStream = session.createUnidirectionalStream().getOutputStream();
    var message = "Unidirectional message";
    uniStream.write(message.getBytes());
    uniStream.close();

    assertTrue(latch.await(15, TimeUnit.SECONDS));
    assertEquals(message, receivedMessage.get());

    session.close();
  }
}
