package io.avaje.jex.ssl;

import java.util.function.Consumer;

import com.sun.net.httpserver.HttpsConfigurator;

import io.avaje.jex.Jex;

final class DSslPlugin implements SslPlugin {

  private final HttpsConfigurator sslConfigurator;

  DSslPlugin(Consumer<SslConfig> consumer) {
    final var config = new DSslConfig();
    consumer.accept(config);
    this.sslConfigurator = SSLConfigurator.create(config);
  }

  @Override
  public void apply(Jex jex) {
    jex.config().httpsConfig(sslConfigurator);
  }
}
