package io.avaje.jex.http3.flupke;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.http3.flupke.webtransport.WebTransportEvent.BiStream;
import io.avaje.jex.ssl.SslPlugin;
import tech.kwik.flupke.Http3Client;
import tech.kwik.flupke.HttpError;
import tech.kwik.flupke.webtransport.ClientSessionFactory;
import tech.kwik.flupke.webtransport.Session;
import tech.kwik.flupke.webtransport.WebTransportStream;

class WebTransportTest {

  @Test
  void test() throws Exception {

    var ssl =
        SslPlugin.create(
            s ->
                s.resourceLoader(getClass())
                    .keystoreFromClasspath("/my-custom-keystore.p12", "password"));

    var webTransport =
        FlupkeJexPlugin.create()
            .connectionConfig(
                b ->
                    b.maxOpenPeerInitiatedUnidirectionalStreams(3)
                        .maxOpenPeerInitiatedBidirectionalStreams(10))
            .webTransport("/", b -> b.onBiDirectionalStream(this::echo));

    var jex =
        Jex.create()
            .plugin(ssl)
            .plugin(webTransport)
            .get(
                "/",
                ctx -> {
                  assertEquals("h3", ctx.exchange().getProtocol());

                  ctx.text("hello world");
                })
            .start();

    var localhost = URI.create("https://localhost:8080/");

    var client =
        (Http3Client)
            Http3Client.newBuilder().disableCertificateCheck().sslContext(ssl.sslContext()).build();

    // ensure regular endpoints work
    assertEquals(
        "hello world",
        client
            .send(
                HttpRequest.newBuilder().timeout(Duration.ofDays(1)).uri(localhost).GET().build(),
                BodyHandlers.ofString())
            .body());

    try {
      var clientSessionFactory =
          ClientSessionFactory.newBuilder().serverUri(localhost).httpClient(client).build();

      int count = clientSessionFactory.getMaxConcurrentSessions();
      for (int i = 0; i < count; i++) {
        Session session = clientSessionFactory.createSession(localhost);
        session.registerSessionTerminatedEventListener(
            (errorCode, message) -> {
              System.out.println(
                  "Session " + session.getSessionId() + " closed with error code " + errorCode);
            });

        session.open();
        System.out.println("Session " + session.getSessionId() + " opened to " + localhost);
        WebTransportStream bidirectionalStream = session.createBidirectionalStream();

        String message = "Hello, world! (" + (i + 1) + ")";
        bidirectionalStream.getOutputStream().write(message.getBytes());
        System.out.println("Request sent to " + localhost + ": " + message);
        bidirectionalStream.getOutputStream().close();
        System.out.print("Response: ");
        bidirectionalStream.getInputStream().transferTo(System.out);
        System.out.println();
        session.close();
        System.out.println("Session closed. ");
      }
      System.out.println("That's it! Bye!");
    } catch (IOException | HttpError e) {
      System.err.println("Request failed: " + e.getMessage());
      e.printStackTrace();
    }
    jex.shutdown();
  }

  private final void echo(BiStream stream) {
    try (stream) {
      stream.requestStream().transferTo(stream.responseStream());

      System.out.println(
          "Processed a request for session " + stream.sessionId() + " response sent");
    } catch (IOException e) {
      System.out.println("IO error while processing request: " + e.getMessage());
    }
  }
}
