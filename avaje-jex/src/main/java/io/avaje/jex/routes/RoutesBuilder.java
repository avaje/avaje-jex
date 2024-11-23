package io.avaje.jex.routes;

import io.avaje.jex.*;
import io.avaje.jex.jdk.JdkFilter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class RoutesBuilder {

  private final EnumMap<Routing.Type, RouteIndex> typeMap = new EnumMap<>(Routing.Type.class);
  private final boolean ignoreTrailingSlashes;
  private final List<JdkFilter> filters = new ArrayList<>();

  public RoutesBuilder(Routing routing, boolean ignoreTrailingSlashes) {
    this.ignoreTrailingSlashes = ignoreTrailingSlashes;
    for (var handler : routing.handlers()) {
      typeMap.computeIfAbsent(handler.getType(), h -> new RouteIndex()).add(convert(handler));
    }
    for (var handler : routing.filters()) {
      filters.add(new JdkFilter(handler));
    }
  }

  private SpiRoutes.Entry convert(Routing.Entry handler) {
    final PathParser pathParser = new PathParser(handler.getPath(), ignoreTrailingSlashes);
    return new RouteEntry(pathParser, handler.getHandler(), handler.getRoles());
  }

  public SpiRoutes build() {
    return new Routes(typeMap, filters);
  }
}
