package io.avaje.jex.http3.flupke.webtransport;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.Jex.Server;
import io.avaje.jex.http3.flupke.FlupkeJexPlugin;
import io.avaje.jex.http3.flupke.webtransport.WebTransportEvent.BiStream;
import io.avaje.jex.ssl.SslPlugin;
import tech.kwik.flupke.Http3Client;
import tech.kwik.flupke.webtransport.ClientSessionFactory;
import tech.kwik.flupke.webtransport.Session;
import tech.kwik.flupke.webtransport.WebTransportStream;

class WebTransportTest {

  private URI localhost = URI.create("https://localhost:8080/");
  private Server jex;
  private static SslPlugin ssl =
      SslPlugin.create(
          s ->
              s.resourceLoader(WebTransportTest.class)
                  .keystoreFromClasspath("/my-custom-keystore.p12", "password"));
  private static Http3Client client =
      (Http3Client)
          Http3Client.newBuilder().disableCertificateCheck().sslContext(ssl.sslContext()).build();

  @AfterEach
  void teardown() throws InterruptedException {
    if (jex != null) {
      jex.shutdown();
    }
  }

  @Test
  void testBasicBidirectionalEcho() throws Exception {
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
    localhost = URI.create("https://localhost:%s/".formatted(jex.port()));
    // Test regular HTTP/3 endpoint
    assertEquals(
        "hello world",
        client
            .send(HttpRequest.newBuilder().uri(localhost).GET().build(), BodyHandlers.ofString())
            .body());

    // Test WebTransport echo
    var clientSessionFactory =
        ClientSessionFactory.newBuilder()
            .serverUri(localhost.resolve("/echo"))
            .httpClient(client)
            .build();

    Session session = clientSessionFactory.createSession(localhost.resolve("/echo"));
    session.open();
    Thread.sleep(Duration.ofMillis(500));
    WebTransportStream bidirectionalStream = session.createBidirectionalStream();
    String message = "Hello, WebTransport!";
    bidirectionalStream.getOutputStream().write(message.getBytes());
    bidirectionalStream.getOutputStream().close();

    ByteArrayOutputStream response = new ByteArrayOutputStream();
    bidirectionalStream.getInputStream().transferTo(response);

    assertEquals(message, response.toString(StandardCharsets.UTF_8));

    session.close();
  }

  @Test
  void testUnidirectionalStream() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<String> receivedMessage = new AtomicReference<>();

    var webTransport =
        FlupkeJexPlugin.create()
            .webTransport(
                "/uni",
                b ->
                    b.onUniDirectionalStream(
                        stream -> {
                          try (stream) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            stream.requestStream().transferTo(baos);
                            receivedMessage.set(baos.toString(StandardCharsets.UTF_8));
                            latch.countDown();
                          } catch (IOException e) {
                            fail("Failed to read unidirectional stream: " + e.getMessage());
                          }
                        }));

    startServer(webTransport);

    var clientSessionFactory =
        ClientSessionFactory.newBuilder()
            .serverUri(localhost.resolve("/uni"))
            .httpClient(client)
            .build();

    Session session = clientSessionFactory.createSession(localhost.resolve("/uni"));
    session.open();

    Thread.sleep(Duration.ofMillis(500));
    OutputStream uniStream = session.createUnidirectionalStream().getOutputStream();
    String message = "Unidirectional message";
    uniStream.write(message.getBytes());
    uniStream.close();

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    assertEquals(message, receivedMessage.get());

    session.close();
  }

  private final void startServer(FlupkeJexPlugin webTransport) {
    jex = Jex.create().plugin(ssl).plugin(webTransport).port(0).start();
    localhost = URI.create("https://localhost:%s/".formatted(jex.port()));
  }

  @Test
  void testServerInitiatedUnidirectionalStream() throws Exception {
    CountDownLatch clientLatch = new CountDownLatch(1);
    AtomicReference<String> clientReceived = new AtomicReference<>();

    var webTransport =
        FlupkeJexPlugin.create()
            .webTransport(
                "/server-uni",
                b ->
                    b.onBiDirectionalStream(
                        stream -> {
                          try {
                            // Read client request
                            ByteArrayOutputStream request = new ByteArrayOutputStream();
                            stream.requestStream().transferTo(request);

                            // Send response via server-initiated unidirectional stream
                            OutputStream uniStream = stream.createUnidirectionalStream();
                            String response =
                                "Server says: " + request.toString(StandardCharsets.UTF_8);
                            uniStream.write(response.getBytes());
                            uniStream.close();

                            stream.close();
                          } catch (IOException e) {
                            fail("Server failed: " + e.getMessage());
                          }
                        }));

    startServer(webTransport);

    var clientSessionFactory =
        ClientSessionFactory.newBuilder()
            .serverUri(localhost.resolve("/server-uni"))
            .httpClient(client)
            .build();

    Session session = clientSessionFactory.createSession(localhost.resolve("/server-uni"));

    // Set up handler for server-initiated unidirectional streams
    session.setUnidirectionalStreamReceiveHandler(
        stream -> {
          try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            stream.getInputStream().transferTo(baos);
            clientReceived.set(baos.toString(StandardCharsets.UTF_8));
            clientLatch.countDown();
          } catch (IOException e) {
            fail("Client failed to read: " + e.getMessage());
          }
        });

    session.open();

    Thread.sleep(Duration.ofMillis(500));
    // Send request
    WebTransportStream biStream = session.createBidirectionalStream();
    biStream.getOutputStream().write("Hello".getBytes());
    biStream.getOutputStream().close();

    assertTrue(clientLatch.await(5, TimeUnit.SECONDS));
    assertEquals("Server says: Hello", clientReceived.get());

    session.close();
  }

  @Test
  void testSessionCloseHandling() throws Exception {
    CountDownLatch openLatch = new CountDownLatch(1);
    CountDownLatch closeLatch = new CountDownLatch(1);
    AtomicLong closeCode = new AtomicLong(-1);
    AtomicReference<String> closeMessage = new AtomicReference<>();

    var webTransport =
        FlupkeJexPlugin.create()
            .webTransport(
                "/close",
                b ->
                    b.onOpen(ctx -> openLatch.countDown())
                        .onClose(
                            ctx -> {
                              closeCode.set(ctx.code());
                              closeMessage.set(ctx.message());
                              closeLatch.countDown();
                            }));

    startServer(webTransport);

    var clientSessionFactory =
        ClientSessionFactory.newBuilder()
            .serverUri(localhost.resolve("/close"))
            .httpClient(client)
            .build();

    Session session = clientSessionFactory.createSession(localhost.resolve("/close"));
    session.open();

    Thread.sleep(Duration.ofMillis(500));
    assertTrue(openLatch.await(5, TimeUnit.SECONDS));

    // Close with custom code and message
    session.close(42, "Test close");

    assertTrue(closeLatch.await(5, TimeUnit.SECONDS));
    assertEquals(42, closeCode.get());
    assertEquals("Test close", closeMessage.get());
  }

  @Test
  void testServerInitiatedClose() throws Exception {
    CountDownLatch clientCloseLatch = new CountDownLatch(1);
    AtomicLong clientCloseCode = new AtomicLong(-1);

    var webTransport =
        FlupkeJexPlugin.create()
            .webTransport(
                "/server-close",
                b ->
                    b.onBiDirectionalStream(
                        stream -> {
                          try (stream) {
                            // Read request
                            stream.requestStream().transferTo(OutputStream.nullOutputStream());
                            // Close the session from server side
                            stream.closeSession(100, "Server initiated close");
                          } catch (IOException e) {
                            // Expected when closing
                          }
                        }));

    startServer(webTransport);

    var clientSessionFactory =
        ClientSessionFactory.newBuilder()
            .serverUri(localhost.resolve("/server-close"))
            .httpClient(client)
            .build();

    Session session = clientSessionFactory.createSession(localhost.resolve("/server-close"));
    session.registerSessionTerminatedEventListener(
        (code, msg) -> {
          clientCloseCode.set(code);
          clientCloseLatch.countDown();
        });

    session.open();
    Thread.sleep(Duration.ofMillis(500));

    WebTransportStream stream = session.createBidirectionalStream();
    stream.getOutputStream().write("trigger close".getBytes());
    stream.getOutputStream().close();

    assertTrue(clientCloseLatch.await(5, TimeUnit.SECONDS));
    assertEquals(100, clientCloseCode.get());
  }

  @Test
  void testMultipleStreamsPerSession() throws Exception {
    AtomicInteger streamCount = new AtomicInteger(0);

    var webTransport =
        FlupkeJexPlugin.create()
            .connectionConfig(b -> b.maxOpenPeerInitiatedBidirectionalStreams(10))
            .webTransport(
                "/multi-stream",
                b ->
                    b.onBiDirectionalStream(
                        stream -> {
                          streamCount.incrementAndGet();
                          this.echo(stream);
                        }));

    startServer(webTransport);

    var clientSessionFactory =
        ClientSessionFactory.newBuilder()
            .serverUri(localhost.resolve("/multi-stream"))
            .httpClient(client)
            .build();

    Session session = clientSessionFactory.createSession(localhost.resolve("/multi-stream"));
    session.open();
    Thread.sleep(Duration.ofMillis(500));

    int numStreams = 5;
    for (int i = 0; i < numStreams; i++) {
      WebTransportStream stream = session.createBidirectionalStream();
      String msg = "Stream " + i;
      stream.getOutputStream().write(msg.getBytes());
      stream.getOutputStream().close();

      ByteArrayOutputStream response = new ByteArrayOutputStream();
      stream.getInputStream().transferTo(response);
      assertEquals(msg, response.toString(StandardCharsets.UTF_8));
    }

    session.close();
    assertEquals(numStreams, streamCount.get());
  }

  @Test
  void testLargeDataTransfer() throws Exception {
    var webTransport =
        FlupkeJexPlugin.create().webTransport("/large", b -> b.onBiDirectionalStream(this::echo));

    startServer(webTransport);

    var clientSessionFactory =
        ClientSessionFactory.newBuilder()
            .serverUri(localhost.resolve("/large"))
            .httpClient(client)
            .build();

    Session session = clientSessionFactory.createSession(localhost.resolve("/large"));
    session.open();

    Thread.sleep(Duration.ofMillis(500));
    // Send 1MB of data
    byte[] largeData = new byte[1024 * 1024];
    for (int i = 0; i < largeData.length; i++) {
      largeData[i] = (byte) (i % 256);
    }

    WebTransportStream stream = session.createBidirectionalStream();
    stream.getOutputStream().write(largeData);
    stream.getOutputStream().close();

    ByteArrayOutputStream response = new ByteArrayOutputStream();
    stream.getInputStream().transferTo(response);
    byte[] received = response.toByteArray();

    assertArrayEquals(largeData, received);

    session.close();
  }

  @Test
  void testPathRetrieval() throws Exception {
    AtomicReference<String> receivedPath = new AtomicReference<>();

    var webTransport =
        FlupkeJexPlugin.create()
            .webTransport(
                "/test-path",
                b ->
                    b.onBiDirectionalStream(
                        stream -> {
                          receivedPath.set(stream.path());
                          try (stream) {
                            stream.requestStream().transferTo(stream.responseStream());
                          } catch (IOException e) {
                            // Ignore
                          }
                        }));

    startServer(webTransport);

    var clientSessionFactory =
        ClientSessionFactory.newBuilder()
            .serverUri(localhost.resolve("/test-path?param=value"))
            .httpClient(client)
            .build();

    Session session =
        clientSessionFactory.createSession(localhost.resolve("/test-path?param=value"));
    session.open();
    Thread.sleep(Duration.ofMillis(500));

    WebTransportStream stream = session.createBidirectionalStream();
    stream.getOutputStream().write("test".getBytes());
    stream.getOutputStream().close();
    stream.getInputStream().transferTo(OutputStream.nullOutputStream());

    session.close();

    assertNotNull(receivedPath.get());
    assertTrue(receivedPath.get().contains("/test-path"));
  }

  @Test
  void testConcurrentBidirectionalStreams() throws Exception {
    AtomicInteger processedStreams = new AtomicInteger(0);

    var webTransport =
        FlupkeJexPlugin.create()
            .connectionConfig(b -> b.maxOpenPeerInitiatedBidirectionalStreams(20))
            .webTransport(
                "/concurrent",
                b ->
                    b.onBiDirectionalStream(
                        stream -> {
                          processedStreams.incrementAndGet();
                          this.echo(stream);
                        }));

    startServer(webTransport);

    var clientSessionFactory =
        ClientSessionFactory.newBuilder()
            .serverUri(localhost.resolve("/concurrent"))
            .httpClient(client)
            .build();

    Session session = clientSessionFactory.createSession(localhost.resolve("/concurrent"));
    session.open();
    Thread.sleep(Duration.ofMillis(500));

    int numStreams = 10;
    List<Thread> threads = new ArrayList<>();

    for (int i = 0; i < numStreams; i++) {
      final int index = i;
      var thread =
          Thread.startVirtualThread(
              () -> {
                try {
                  WebTransportStream stream = session.createBidirectionalStream();
                  String msg = "Concurrent " + index;
                  stream.getOutputStream().write(msg.getBytes());
                  stream.getOutputStream().close();

                  ByteArrayOutputStream response = new ByteArrayOutputStream();
                  stream.getInputStream().transferTo(response);
                  assertEquals(msg, response.toString(StandardCharsets.UTF_8));
                } catch (IOException e) {
                  fail("Thread " + index + " failed: " + e.getMessage());
                }
              });
      threads.add(thread);
    }

    // Wait for all threads
    for (var thread : threads) {
      thread.join();
    }

    session.close();
    assertEquals(numStreams, processedStreams.get());
  }

  private void echo(BiStream stream) {
    try (stream) {
      stream.requestStream().transferTo(stream.responseStream());
    } catch (IOException e) {
      System.err.println("IO error while processing request: " + e.getMessage());
    }
  }
}
