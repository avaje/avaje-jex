package io.avaje.jex.routes;

import java.util.EnumMap;

public class RoutesBuilder {

  private final EnumMap<HandlerType, RouteIndex> typeMap = new EnumMap<>(HandlerType.class);

  public RoutesBuilder() {
    final WebApiEntries all = WebApi.all();
    for (WebApiEntry handler : all.getHandlers()) {
      typeMap.computeIfAbsent(handler.getType(), h -> new RouteIndex()).add(convert(handler));
    }
    //TODO: grab all before & after filters
    all.clear();
  }

  private RouteEntry convert(WebApiEntry handler) {
    final PathParser pathParser = new PathParser(handler.getPath());
    return new RouteEntry(pathParser, handler);
  }

  public Routes build() {
    return new Routes(typeMap);
  }
}
