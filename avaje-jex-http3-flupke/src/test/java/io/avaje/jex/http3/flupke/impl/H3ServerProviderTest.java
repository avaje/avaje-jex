package io.avaje.jex.http3.flupke.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.http.HttpClient.Version;

import org.junit.jupiter.api.Test;

import io.avaje.http.client.HttpClient;
import io.avaje.jex.Jex;
import io.avaje.jex.http3.flupke.FlupkeJexPlugin;
import io.avaje.jex.ssl.SslPlugin;

class H3ServerProviderTest {

  @Test
  void test() throws Exception {

    var ssl =
        SslPlugin.create(
            s ->
                s.resourceLoader(getClass())
                    .keystoreFromClasspath("/my-custom-keystore.p12", "password"));

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
        HttpClient.builder()
            .version(Version.valueOf("HTTP_3"))
            .baseUrl("https://localhost:8080")
            .sslContext(ssl.sslContext())
            .build()
            .request()
            .GET()
            .asString()
            .body();

    assertEquals("hello world", body);

    jex.shutdown();
  }
}
