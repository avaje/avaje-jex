package io.avaje.jex.http3.flupke;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.ssl.SslPlugin;
import tech.kwik.flupke.Http3Client;

class Http3Test {

  @Test
  void test() throws Exception {

    var ssl =
        SslPlugin.create(
            s -> s.resourceLoader(getClass()).keystoreFromClasspath("/keystore.p12", "password"));

    var jex =
        Jex.create()
            .plugin(ssl)
            .plugin(FlupkeJexPlugin.create())
            .get(
                "/",
                ctx -> {
                  assertEquals("h3", ctx.exchange().getProtocol());

                  ctx.text("hello world");
                })
            .start();
    var body =
        (Runtime.version().feature() >= 26
                ? HttpClient.newBuilder().version(Version.valueOf("HTTP_3"))
                : Http3Client.newBuilder())
            .sslContext(ssl.sslContext())
            .build()
            .send(
                HttpRequest.newBuilder().uri(URI.create("https://localhost:8080")).GET().build(),
                BodyHandlers.ofString())
            .body();

    assertEquals("hello world", body);

    jex.shutdown();
  }
}
