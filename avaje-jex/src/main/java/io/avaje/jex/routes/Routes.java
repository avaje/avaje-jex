package io.avaje.jex.routes;

import java.lang.System.Logger.Level;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import io.avaje.jex.Routing;
import io.avaje.jex.http.HttpFilter;

final class Routes implements SpiRoutes {

  private static final System.Logger log = System.getLogger("io.avaje.jex");

  /**
   * The "real" handlers by http method.
   */
  private final EnumMap<Routing.Type, RouteIndex> typeMap;

  /**
   * The filters.
   */
  private final List<HttpFilter> filters;

  private final AtomicLong noRouteCounter = new AtomicLong();

  Routes(EnumMap<Routing.Type, RouteIndex> typeMap, List<HttpFilter> filters) {
    this.typeMap = typeMap;
    this.filters = filters;
  }

  @Override
  public String toString() {
    return "Routes{" + typeMap + ", filters=" + filters + '}';
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
  public void waitForIdle(long maxSeconds) {
    log.log(Level.DEBUG, "stopping server with maxWaitSeconds {0}", maxSeconds);
    maxWaitAttempts(maxSeconds * 20);  // 50 millis per attempt
    park50Millis();
    if (!maxWaitAttempts(5)) {
      log.log(Level.WARNING, "Active requests still in process");
    }
  }

  private boolean maxWaitAttempts(final long maxAttempts) {
    long attempts = 0;
    while ((activeRequests()) > 0 && ++attempts < maxAttempts) {
      park50Millis();
    }
    return attempts < maxAttempts;
  }

  private void park50Millis() {
    LockSupport.parkNanos(50_000_000);
  }

  @Override
  public Entry match(Routing.Type type, String pathInfo) {
    return typeMap.get(type).match(pathInfo);
  }

  @Override
  public List<HttpFilter> filters() {
    return filters;
  }
}
