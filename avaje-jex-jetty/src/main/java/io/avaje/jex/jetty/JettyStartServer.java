package io.avaje.jex.jetty;

import io.avaje.jex.Jex;
import io.avaje.jex.spi.SpiRoutes;
import io.avaje.jex.spi.SpiServiceManager;
import io.avaje.jex.spi.SpiStartServer;

/**
 * Configure and starts the underlying Jetty server.
 */
public class JettyStartServer implements SpiStartServer {

  @Override
  public Jex.Server start(Jex jex, SpiRoutes routes, SpiServiceManager serviceManager) {
    return new JettyLaunch(jex, routes, serviceManager)
      .start();
  }
}
