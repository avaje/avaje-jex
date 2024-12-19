package io.avaje.jex.routes;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;

import io.avaje.jex.HttpFilter;
import io.avaje.jex.JexConfig;
import io.avaje.jex.Routing;

public final class RoutesBuilder {

  private final EnumMap<Routing.Type, RouteIndex> typeMap = new EnumMap<>(Routing.Type.class);
  private final boolean ignoreTrailingSlashes;
  private final List<HttpFilter> filters;
  private final String contextPath;

  public RoutesBuilder(Routing routing, JexConfig config) {
    this.ignoreTrailingSlashes = config.ignoreTrailingSlashes();
    final var buildMap = new LinkedHashMap<Routing.Type, RouteIndexBuild>();
    this.contextPath = config.contextPath().transform(s -> "/".equals(s) ? "" : s);
    for (var handler : routing.handlers()) {
      buildMap.computeIfAbsent(handler.getType(), h -> new RouteIndexBuild()).add(convert(handler));
    }
    buildMap.forEach((key, value) -> typeMap.put(key, value.build()));
    filters = List.copyOf(routing.filters());
  }

  private SpiRoutes.Entry convert(Routing.Entry handler) {
    final PathParser pathParser =
        new PathParser(contextPath + handler.getPath(), ignoreTrailingSlashes);
    return new RouteEntry(pathParser, handler.getHandler(), handler.getRoles());
  }

  public SpiRoutes build() {
    return new Routes(typeMap, filters);
  }
}
