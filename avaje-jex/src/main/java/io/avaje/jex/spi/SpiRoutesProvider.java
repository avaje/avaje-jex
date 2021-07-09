package io.avaje.jex.spi;

import io.avaje.jex.AccessManager;
import io.avaje.jex.Routing;

public interface SpiRoutesProvider {

  /**
   * Build and return the Routing.
   */
  SpiRoutes create(Routing routing, AccessManager accessManager, boolean ignoreTrailingSlashes);

}
