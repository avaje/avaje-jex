package io.avaje.jex.routes;

import io.avaje.jex.Routing;
import io.avaje.jex.spi.SpiContext;
import io.avaje.jex.spi.SpiRoutes;

import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

class Routes implements SpiRoutes {

  /**
   * The "real" handlers by http method.
   */
  private final EnumMap<Routing.Type, RouteIndex> typeMap;

  /**
   * The before filters.
   */
  private final List<Entry> before;

  /**
   * The after filters.
   */
  private final List<Entry> after;

  private final AtomicLong noRouteCounter = new AtomicLong();

  Routes(EnumMap<Routing.Type, RouteIndex> typeMap, List<Entry> before, List<Entry> after) {
    this.typeMap = typeMap;
    this.before = before;
    this.after = after;
  }

  @Override
  public void inc() {
    noRouteCounter.incrementAndGet();
  }

  @Override
  public void dec() {
    noRouteCounter.decrementAndGet();
  }

  @Override
  public long activeRequests() {
    long total = noRouteCounter.get();
    for (RouteIndex value : typeMap.values()) {
      total += value.activeRequests();
    }
    return total;
  }

  @Override
  public Entry match(Routing.Type type, String pathInfo) {
    return typeMap.get(type).match(pathInfo);
  }

  @Override
  public void before(String pathInfo, SpiContext ctx) {
    ctx.setMode(Routing.Type.BEFORE);
    for (Entry beforeFilter : before) {
      if (beforeFilter.matches(pathInfo)) {
        beforeFilter.handle(ctx);
      }
    }
  }

  @Override
  public void after(String pathInfo, SpiContext ctx) {
    ctx.setMode(Routing.Type.AFTER);
    for (Entry afterFilter : after) {
      if (afterFilter.matches(pathInfo)) {
        afterFilter.handle(ctx);
      }
    }
  }
}
