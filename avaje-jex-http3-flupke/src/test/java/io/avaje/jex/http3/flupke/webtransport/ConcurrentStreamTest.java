package io.avaje.jex.http3.flupke.webtransport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import io.avaje.jex.http3.flupke.FlupkeJexPlugin;
import tech.kwik.flupke.webtransport.ClientSessionFactory;

class ConcurrentStreamTest extends WebTransportBaseTest {

  @Test
  void testConcurrentBidirectionalStreams() throws Exception {
    var processedStreams = new AtomicInteger(0);

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

    var session = clientSessionFactory.createSession(localhost.resolve("/concurrent"));
    session.open();

    var numStreams = 10;
    List<Thread> threads = new ArrayList<>();

    for (var i = 0; i < numStreams; i++) {
      final var index = i;
      var thread =
          Thread.startVirtualThread(
              () -> {
                try {
                  var stream = session.createBidirectionalStream();
                  var msg = "Concurrent " + index;
                  stream.getOutputStream().write(msg.getBytes());
                  stream.getOutputStream().close();

                  var response = new ByteArrayOutputStream();
                  stream.getInputStream().transferTo(response);
                  assertEquals(msg, response.toString(java.nio.charset.StandardCharsets.UTF_8));
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
}
