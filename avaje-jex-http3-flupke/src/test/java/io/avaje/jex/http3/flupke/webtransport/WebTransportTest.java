package io.avaje.jex.http3.flupke.webtransport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.avaje.applog.AppLog;
import io.avaje.jex.Jex;
import io.avaje.jex.Jex.Server;
import io.avaje.jex.http3.flupke.FlupkeJexPlugin;
import io.avaje.jex.http3.flupke.webtransport.WebTransportEvent.BiStream;
import io.avaje.jex.ssl.SslPlugin;
import tech.kwik.core.log.SysOutLogger;
import tech.kwik.flupke.Http3Client;
import tech.kwik.flupke.webtransport.ClientSessionFactory;

class WebTransportTest {

  private static URI localhost;
  private static Logger logger = AppLog.getLogger(WebTransportTest.class);

  private static Server jex;
  private static SslPlugin ssl =
      SslPlugin.create(
          s ->
              s.resourceLoader(WebTransportTest.class)
                  .keystoreFromClasspath("/keystore.p12", "password"));

  private Http3Client client =
      (Http3Client)
          Http3Client.newBuilder().sslContext(ssl.sslContext()).build();
  private static CountDownLatch uniLatch = new CountDownLatch(1);
  private static AtomicReference<String> receivedMessage = new AtomicReference<>();

  @AfterEach
  void teardown() {
    logger.log(Level.INFO, "Shutting down");
    jex.shutdown();
  }

  @Test
  void testBasicBidirectionalEcho() throws Exception {
    var log = new SysOutLogger();
    log.logWarning(true);
    var webTransport =
        FlupkeJexPlugin.create()
            .connectorConfig(b -> b.withLogger(log))
            .webTransport(
                "/",
                b ->
                    b.onBiDirectionalStream(WebTransportTest::echo)
                        .onUniDirectionalStream(
                            stream -> {
                              try (stream) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                stream.requestStream().transferTo(baos);
                                receivedMessage.set(baos.toString(StandardCharsets.UTF_8));
                                uniLatch.countDown();
                              } catch (IOException e) {
                                fail("Failed to read unidirectional stream: " + e.getMessage());
                              }
                            })
                        .onClose(
                            c -> {
                              logger.log(Level.INFO, "Closed With " + c.message);
                            })
                        .onOpen(o -> logger.log(Level.INFO, "session:" + o.sessionId())));
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

    var clientSessionFactory =
        ClientSessionFactory.newBuilder().serverUri(localhost).httpClient(client).build();

    var session = clientSessionFactory.createSession(localhost);
    session.open();
    var bidirectionalStream = session.createBidirectionalStream();
    var message = "Hello, WebTransport!";

    logger.log(Level.INFO, "Sending Bi Request");
    bidirectionalStream.getOutputStream().write(message.getBytes());
    bidirectionalStream.getOutputStream().close();
    var response = new ByteArrayOutputStream();
    bidirectionalStream.getInputStream().transferTo(response);
    assertEquals(message, response.toString(StandardCharsets.UTF_8));

    var uniStream = session.createUnidirectionalStream().getOutputStream();

    message = "Unidirectional message";
    logger.log(Level.INFO, "Sending Uni Request");
    uniStream.write(message.getBytes());
    uniStream.close();

    assertTrue(uniLatch.await(15, TimeUnit.SECONDS));
    assertEquals(message, receivedMessage.get());
    logger.log(Level.INFO, "Closing Session");
    session.close();
  }

  private static void echo(BiStream stream) {

    try {
      logger.log(Level.INFO, "Recieved request");
      stream.requestStream().transferTo(stream.responseStream());
      logger.log(Level.INFO, "read request");
      stream.close();
      logger.log(Level.INFO, "sent response");
      Thread.sleep(5000);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
