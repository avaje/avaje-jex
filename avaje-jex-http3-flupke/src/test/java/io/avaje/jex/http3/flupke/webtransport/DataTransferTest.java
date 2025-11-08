package io.avaje.jex.http3.flupke.webtransport;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import io.avaje.jex.http3.flupke.FlupkeJexPlugin;
import tech.kwik.flupke.webtransport.ClientSessionFactory;

class DataTransferTest extends WebTransportBaseTest {

  @Test
  void testMultipleStreamsPerSession() throws Exception {
    var streamCount = new AtomicInteger(0);

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

    var session = clientSessionFactory.createSession(localhost.resolve("/multi-stream"));
    session.open();

    var numStreams = 5;
    for (var i = 0; i < numStreams; i++) {
      var stream = session.createBidirectionalStream();
      var msg = "Stream " + i;
      stream.getOutputStream().write(msg.getBytes());
      stream.getOutputStream().close();

      var response = new ByteArrayOutputStream();
      stream.getInputStream().transferTo(response);
      assertEquals(msg, response.toString(java.nio.charset.StandardCharsets.UTF_8));
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

    var session = clientSessionFactory.createSession(localhost.resolve("/large"));
    session.open();

    // Send 1MB of data
    var largeData = new byte[1024 * 1024];
    for (var i = 0; i < largeData.length; i++) {
      largeData[i] = (byte) (i % 256);
    }

    var stream = session.createBidirectionalStream();
    stream.getOutputStream().write(largeData);
    stream.getOutputStream().close();

    var response = new ByteArrayOutputStream();
    stream.getInputStream().transferTo(response);
    var received = response.toByteArray();

    assertArrayEquals(largeData, received);

    session.close();
  }
}
