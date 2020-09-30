package io.avaje.jex.jetty;

import io.avaje.jex.Jex;
import io.avaje.jex.spi.SpiServer;

public class JettySpiServer implements SpiServer {

  @Override
  public Jex.Server start(Jex jex) {
    return new JettyLaunch(jex).start();
  }
}
