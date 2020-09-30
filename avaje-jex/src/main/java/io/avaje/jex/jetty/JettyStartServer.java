package io.avaje.jex.jetty;

import io.avaje.jex.Jex;
import io.avaje.jex.spi.SpiStartServer;

/**
 * Configure and starts the underlying Jetty server.
 */
public class JettyStartServer implements SpiStartServer {

  @Override
  public Jex.Server start(Jex jex) {
    return new JettyLaunch(jex).start();
  }
}
