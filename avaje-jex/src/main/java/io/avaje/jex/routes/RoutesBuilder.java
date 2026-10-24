package io.avaje.jex.routes;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;

import io.avaje.jex.JexConfig;
import io.avaje.jex.Routing;
import io.avaje.jex.http.HttpFilter;

public final class RoutesBuilder {

  private final EnumMap<Routing.Type, RouteIndex> typeMap = new EnumMap<>(Routing.Type.class);
  private final boolean ignoreTrailingSlashes;
  private final List<HttpFilter> filters;
  private final String contextPath;

  public RoutesBuilder(Routing routing, JexConfig config) {
    this.ignoreTrailingSlashes = config.ignoreTrailingSlashes();
    final var buildMap = new LinkedHashMap<Routing.Type, RouteIndexBuild>();
    this.contextPath = config.contextPath().transform(s -> "/".equals(s) ? "" : s);
    this.filters = routing.filters().stream().map(this::bind).toList();
    for (var handler : routing.handlers()) {
      buildMap.computeIfAbsent(handler.getType(), h -> new RouteIndexBuild()).add(convert(handler));
    }
    buildMap.forEach((key, value) -> typeMap.put(key, value.build()));
  }

  private HttpFilter bind(HttpFilter filter) {
    return filter instanceof PathFilter pathFilter
        ? pathFilter.bind(contextPath, ignoreTrailingSlashes)
        : filter;
  }

  private SpiRoutes.Entry convert(Routing.Entry handler) {
    final PathParser pathParser =
        new PathParser(contextPath + handler.getPath(), ignoreTrailingSlashes);
    return new RouteEntry(
        pathParser, handler.getHandler(), handler.getRoles(), routeFilters(pathParser.raw()));
  }

  private List<HttpFilter> routeFilters(String routePath) {
    if (!isStatic(routePath)) {
      return filters;
    }
    final var result = new ArrayList<HttpFilter>(filters.size());
    for (HttpFilter filter : filters) {
      if (filter instanceof PathFilter pathFilter) {
        if (pathFilter.matches(routePath)) {
          result.add(pathFilter.delegate());
        }
      } else {
        result.add(filter);
      }
    }
    return List.copyOf(result);
  }

  private static boolean isStatic(String path) {
    return path.indexOf('{') == -1 && path.indexOf('<') == -1 && path.indexOf('*') == -1;
  }

  public SpiRoutes build() {
    return new Routes(typeMap);
  }
}
