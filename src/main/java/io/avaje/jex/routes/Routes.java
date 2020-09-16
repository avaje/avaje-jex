package io.avaje.jex.routes;

import java.util.EnumMap;

public class Routes {

  private final EnumMap<HandlerType, RouteIndex> typeMap;

  public Routes(EnumMap<HandlerType, RouteIndex> typeMap) {
    this.typeMap = typeMap;
  }

  public RouteEntry match(HandlerType type, String pathInfo) {
    return typeMap.get(type).match(pathInfo);
  }
}
