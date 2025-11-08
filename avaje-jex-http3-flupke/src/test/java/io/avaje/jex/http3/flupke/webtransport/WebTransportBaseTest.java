package io.avaje.jex.http3.flupke.webtransport;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URI;

import org.junit.jupiter.api.AfterEach;

import io.avaje.jex.Jex;
import io.avaje.jex.Jex.Server;
import io.avaje.jex.http3.flupke.FlupkeJexPlugin;
import io.avaje.jex.http3.flupke.webtransport.WebTransportEvent.BiStream;
import io.avaje.jex.ssl.SslPlugin;
import tech.kwik.flupke.Http3Client;
import tech.kwik.flupke.webtransport.ClientSessionFactory;

public abstract class WebTransportBaseTest {

  protected URI localhost;
  protected Server jex;

  static final SslPlugin ssl =
      SslPlugin.create(
          s ->
              s.resourceLoader(WebTransportBaseTest.class)
                  .keystoreFromClasspath("/my-custom-keystore.p12", "password"));
  Http3Client client =
      (Http3Client)
          Http3Client.newBuilder().disableCertificateCheck().sslContext(ssl.sslContext()).build();

  @AfterEach
  void teardown() {
    if (jex != null) {
      jex.shutdown();
    }
    client.close();
  }

  protected final void startServer(FlupkeJexPlugin webTransport) {
    jex = Jex.create().plugin(ssl).plugin(webTransport).port(0).start();
    localhost = URI.create("https://localhost:%s/".formatted(jex.port()));
  }

  protected void echo(BiStream stream) {
    try (stream) {
      // Echoes the request body back as the response body
      stream.requestStream().transferTo(stream.responseStream());
      System.err.println("Wrote Echo");
    } catch (IOException e) {
      fail("IO error while processing request: " + e.getMessage());
    }
  }

  ClientSessionFactory createClientSessionFactory(String path) throws IOException {
    return ClientSessionFactory.newBuilder()
        .serverUri(localhost.resolve(path))
        .httpClient(client)
        .build();
  }
}
