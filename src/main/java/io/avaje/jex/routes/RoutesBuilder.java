package io.avaje.jex.routes;

import io.avaje.jex.Routing;
import io.avaje.jex.spi.SpiRoutes;

import java.util.EnumMap;
import java.util.List;

public class RoutesBuilder {

  private final EnumMap<Routing.Type, RouteIndex> typeMap = new EnumMap<>(Routing.Type.class);

  public RoutesBuilder(Routing routing) {
    final List<Routing.Entry> all = routing.all();
    //TODO: filter before/after
    for (Routing.Entry handler : all) {
      typeMap.computeIfAbsent(handler.getType(), h -> new RouteIndex()).add(convert(handler));
    }
  }

  private SpiRoutes.Entry convert(Routing.Entry handler) {
    final PathParser pathParser = new PathParser(handler.getPath());
    return new RouteEntry(pathParser, handler);
  }

  public SpiRoutes build() {
    return new Routes(typeMap);
  }
}
