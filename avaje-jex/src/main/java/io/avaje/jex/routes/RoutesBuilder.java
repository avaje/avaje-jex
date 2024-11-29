package io.avaje.jex.routes;

import java.util.EnumMap;
import java.util.List;

import io.avaje.jex.HttpFilter;
import io.avaje.jex.Routing;

public final class RoutesBuilder {

  private final EnumMap<Routing.Type, RouteIndex> typeMap = new EnumMap<>(Routing.Type.class);
  private final boolean ignoreTrailingSlashes;
  private final List<HttpFilter> filters;

  public RoutesBuilder(Routing routing, boolean ignoreTrailingSlashes) {
    this.ignoreTrailingSlashes = ignoreTrailingSlashes;
    for (var handler : routing.handlers()) {
      typeMap.computeIfAbsent(handler.getType(), h -> new RouteIndex()).add(convert(handler));
    }
    filters = List.copyOf(routing.filters());
  }

  private SpiRoutes.Entry convert(Routing.Entry handler) {
    final PathParser pathParser = new PathParser(handler.getPath(), ignoreTrailingSlashes);
    return new RouteEntry(pathParser, handler.getHandler(), handler.getRoles());
  }

  public SpiRoutes build() {
    return new Routes(typeMap, filters);
  }
}
