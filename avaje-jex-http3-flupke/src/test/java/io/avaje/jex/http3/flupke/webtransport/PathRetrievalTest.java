package io.avaje.jex.http3.flupke.webtransport;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import io.avaje.jex.http3.flupke.FlupkeJexPlugin;
import tech.kwik.flupke.webtransport.ClientSessionFactory;

class PathRetrievalTest extends WebTransportBaseTest {

  @Test
  void testPathRetrieval() throws Exception {
    var receivedPath = new AtomicReference<String>();

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

    var session =
        clientSessionFactory.createSession(localhost.resolve("/test-path?param=value"));
    session.open();

    var stream = session.createBidirectionalStream();
    stream.getOutputStream().write("test".getBytes());
    stream.getOutputStream().close();
    // Read response to ensure server code executes
    stream.getInputStream().transferTo(OutputStream.nullOutputStream());

    session.close();

    assertNotNull(receivedPath.get());
    // The path should include the query parameters if the underlying implementation exposes it.
    // Assuming it includes the path portion before query.
    assertTrue(receivedPath.get().startsWith("/test-path"));
  }
}
