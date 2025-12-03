package io.avaje.jex.http3.flupke;

import io.avaje.jex.Jex;
import io.avaje.jex.ssl.SslPlugin;

public class Main {

  public static void main(String[] args) {
    SslPlugin sslPlugin =
        SslPlugin.create(
            s ->
                s.resourceLoader(TestPair.class)
                    .keystoreFromClasspath("/my-custom-keystore.p12", "password"));
    Jex.create()
        .get("/", ctx -> ctx.text("hello world"))
        .plugin(sslPlugin)
        .plugin(FlupkeJexPlugin.create())
        .port(8080)
        .start();
  }
}
