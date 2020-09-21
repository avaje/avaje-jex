package io.avaje.jex.routes;

import io.avaje.jex.Routing;
import io.avaje.jex.spi.SpiRoutes;

import java.util.EnumMap;

class Routes implements SpiRoutes {

  private final EnumMap<Routing.Type, RouteIndex> typeMap;

  public Routes(EnumMap<Routing.Type, RouteIndex> typeMap) {
    this.typeMap = typeMap;
  }

  @Override
  public Entry match(Routing.Type type, String pathInfo) {
    return typeMap.get(type).match(pathInfo);
  }
}
