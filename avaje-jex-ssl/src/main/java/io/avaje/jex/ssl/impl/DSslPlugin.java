package io.avaje.jex.ssl.impl;

import java.util.function.Consumer;

import javax.net.ssl.SSLContext;

import com.sun.net.httpserver.HttpsConfigurator;

import io.avaje.jex.Jex;
import io.avaje.jex.ssl.SslConfig;
import io.avaje.jex.ssl.SslPlugin;

public final class DSslPlugin implements SslPlugin {

  private final HttpsConfigurator sslConfigurator;

  public DSslPlugin(Consumer<SslConfig> consumer) {
    final var config = new DSslConfig();
    consumer.accept(config);
    this.sslConfigurator = SSLConfigurator.create(config);
  }

  @Override
  public void apply(Jex jex) {
    jex.config().httpsConfig(sslConfigurator);
  }

  @Override
  public SSLContext sslContext() {
    return sslConfigurator.getSSLContext();
  }
}
