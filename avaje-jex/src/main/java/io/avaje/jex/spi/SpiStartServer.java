package io.avaje.jex.spi;

import io.avaje.jex.Jex;

/**
 * Start the server.
 */
public interface SpiStartServer extends JexExtension {

  /**
   * Return the started server.
   */
  Jex.Server start(Jex jex, SpiRoutes routes, SpiServiceManager serviceManager);

}
