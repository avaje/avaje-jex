package io.avaje.jex.routes;

import io.avaje.jex.AccessManager;
import io.avaje.jex.Routing;
import io.avaje.jex.spi.SpiRoutes;
import io.avaje.jex.spi.SpiRoutesProvider;

public class BootstrapRoutes implements SpiRoutesProvider {

  @Override
  public SpiRoutes create(Routing routing, AccessManager accessManager, boolean ignoreTrailingSlashes) {
    return new RoutesBuilder(routing, accessManager, ignoreTrailingSlashes).build();
  }
}
