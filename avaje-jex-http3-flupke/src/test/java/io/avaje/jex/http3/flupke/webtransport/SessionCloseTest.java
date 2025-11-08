package io.avaje.jex.http3.flupke.webtransport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import io.avaje.jex.http3.flupke.FlupkeJexPlugin;
import tech.kwik.flupke.webtransport.ClientSessionFactory;

class SessionCloseTest extends WebTransportBaseTest {

  @Test
  void testClientInitiatedSessionCloseHandling() throws Exception {
    var openLatch = new CountDownLatch(1);
    var closeLatch = new CountDownLatch(1);
    var closeCode = new AtomicLong(-1);
    var closeMessage = new AtomicReference<String>();

    var webTransport =
        FlupkeJexPlugin.create()
            .webTransport(
                "/close",
                b ->
                    b.onOpen(__ -> openLatch.countDown())
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

    var session = clientSessionFactory.createSession(localhost.resolve("/close"));
    session.open();

    assertTrue(openLatch.await(15, TimeUnit.SECONDS));

    // Close with custom code and message
    session.close(42, "Test close");

    assertTrue(closeLatch.await(15, TimeUnit.SECONDS));
    assertEquals(42, closeCode.get());
    assertEquals("Test close", closeMessage.get());
  }

  @Test
  void testServerInitiatedSessionClose() throws Exception {
    var clientCloseLatch = new CountDownLatch(1);
    var clientCloseCode = new AtomicLong(-1);

    var webTransport =
        FlupkeJexPlugin.create()
            .webTransport(
                "/server-close",
                b ->
                    b.onBiDirectionalStream(
                        stream -> {
                          try (stream) {
                            // Read request to ensure session is active
                            stream.requestStream().transferTo(OutputStream.nullOutputStream());
                            // Close the session from server side
                            stream.closeSession(100, "Server initiated close");
                          } catch (IOException e) {
                            // Expected when closing
                          }
                        }));

    startServer(webTransport);

    var clientSessionFactory = createClientSessionFactory("/server-close");

    var session = clientSessionFactory.createSession(localhost.resolve("/server-close"));
    session.registerSessionTerminatedEventListener(
        (code, __) -> {
          clientCloseCode.set(code);
          clientCloseLatch.countDown();
        });

    session.open();

    // Trigger the server to close the session
    var stream = session.createBidirectionalStream();
    stream.getOutputStream().write("trigger close".getBytes());
    stream.getOutputStream().close();

    assertTrue(clientCloseLatch.await(15, TimeUnit.SECONDS));
    assertEquals(100, clientCloseCode.get());
  }
}
