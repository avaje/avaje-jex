package io.avaje.jex.routes;

import io.avaje.jex.Routing;
import io.avaje.jex.spi.SpiRoutes;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class RoutesBuilder {

  private final EnumMap<Routing.Type, RouteIndex> typeMap = new EnumMap<>(Routing.Type.class);
  private final List<SpiRoutes.Entry> before = new ArrayList<>();
  private final List<SpiRoutes.Entry> after = new ArrayList<>();

  public RoutesBuilder(Routing routing) {
    for (Routing.Entry handler : routing.all()) {
      switch (handler.getType()) {
        case BEFORE:
          before.add(filter(handler));
          break;
        case AFTER:
          after.add(filter(handler));
          break;
        default:
          typeMap.computeIfAbsent(handler.getType(), h -> new RouteIndex()).add(convert(handler));
      }
    }
  }

  private SpiRoutes.Entry filter(Routing.Entry entry) {
    return new FilterEntry(entry);
  }

  private SpiRoutes.Entry convert(Routing.Entry handler) {
    final PathParser pathParser = new PathParser(handler.getPath());
    return new RouteEntry(pathParser, handler);
  }

  public SpiRoutes build() {
    return new Routes(typeMap, before, after);
  }
}
