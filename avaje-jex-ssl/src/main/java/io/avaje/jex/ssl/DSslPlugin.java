package io.avaje.jex.ssl;

import java.util.function.Consumer;

import javax.net.ssl.SSLContext;

import com.sun.net.httpserver.HttpsConfigurator;

import io.avaje.jex.Jex;

final class DSslPlugin implements SslPlugin {

  final HttpsConfigurator sslConfigurator;

  DSslPlugin(Consumer<SslConfig> consumer) {

    final var config = new DSslConfig();

    consumer.accept(config);
    sslConfigurator = SSLContextFactory.getSslContext(config);
  }

  DSslPlugin(SSLContext context) {

    this.sslConfigurator = new HttpsConfigurator(context);
  }

  @Override
  public void apply(Jex jex) {

    jex.config().httpsConfig(sslConfigurator);
  }
}
