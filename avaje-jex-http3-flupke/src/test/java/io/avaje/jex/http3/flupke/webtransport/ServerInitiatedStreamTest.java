package io.avaje.jex.http3.flupke.webtransport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import io.avaje.jex.http3.flupke.FlupkeJexPlugin;

class ServerInitiatedStreamTest extends WebTransportBaseTest {

  @Test
  void testServerInitiatedUnidirectionalStream() throws Exception {
    var clientLatch = new CountDownLatch(1);
    var clientReceived = new AtomicReference<String>();

    var webTransport =
        FlupkeJexPlugin.create()
            .webTransport(
                "/server-uni",
                b ->
                    b.onBiDirectionalStream(
                        stream -> {
                          try {
                            // Read client request
                            var request = new ByteArrayOutputStream();
                            stream.requestStream().transferTo(request);

                            // Send response via server-initiated unidirectional stream
                            var uniStream = stream.createUnidirectionalStream();
                            var response =
                                "Server says: " + request.toString(StandardCharsets.UTF_8);
                            uniStream.write(response.getBytes());
                            uniStream.close();

                            stream.close();
                          } catch (IOException e) {
                            fail("Server failed: " + e.getMessage());
                          }
                        }));

    startServer(webTransport);

    var clientSessionFactory = createClientSessionFactory("/server-uni");

    var session = clientSessionFactory.createSession(localhost.resolve("/server-uni"));

    // Set up handler for server-initiated unidirectional streams
    session.setUnidirectionalStreamReceiveHandler(
        stream -> {
          try {
            var baos = new ByteArrayOutputStream();
            stream.getInputStream().transferTo(baos);
            clientReceived.set(baos.toString(StandardCharsets.UTF_8));
            clientLatch.countDown();
          } catch (IOException e) {
            fail("Client failed to read: " + e.getMessage());
          }
        });

    session.open();

    // Send request
    var biStream = session.createBidirectionalStream();
    biStream.getOutputStream().write("Hello".getBytes());
    biStream.getOutputStream().close();

    assertTrue(clientLatch.await(15, TimeUnit.SECONDS));
    assertEquals("Server says: Hello", clientReceived.get());

    session.close();
  }
}
