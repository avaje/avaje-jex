package io.avaje.jex.ssl.core;

import java.util.function.Consumer;

import io.avaje.jex.Jex;
import io.avaje.jex.ssl.SSLConfigurator;
import io.avaje.jex.ssl.SslConfig;
import io.avaje.jex.ssl.SslPlugin;

public final class DSslPlugin implements SslPlugin {

  private final DConfigurator sslConfigurator;

  public DSslPlugin(Consumer<SslConfig> consumer) {
    final var config = new DSslConfig();
    consumer.accept(config);
    this.sslConfigurator = DConfigurator.create(config);
  }

  @Override
  public void apply(Jex jex) {
    jex.config().httpsConfig(sslConfigurator);
  }

  @Override
  public SSLConfigurator sslConfigurator() {
    return sslConfigurator;
  }
}
