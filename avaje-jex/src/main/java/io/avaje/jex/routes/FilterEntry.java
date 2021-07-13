package io.avaje.jex.routes;

import io.avaje.jex.Context;
import io.avaje.jex.Handler;
import io.avaje.jex.Routing;
import io.avaje.jex.spi.SpiRoutes;

import java.util.Map;

/**
 * Filter with special matchAll.
 */
class FilterEntry implements SpiRoutes.Entry {

  private final String path;
  private final boolean matchAll;
  private final PathParser pathParser;
  private final Handler handler;

  FilterEntry(Routing.Entry entry, boolean ignoreTrailingSlashes) {
    this.path = entry.getPath();
    this.matchAll = "/*".equals(path) || "*".equals(path);
    this.pathParser = matchAll ? null : new PathParser(path, ignoreTrailingSlashes);
    this.handler = entry.getHandler();
  }

  @Override
  public void inc() {
    // do nothing
  }

  @Override
  public void dec() {
    // do nothing
  }

  @Override
  public long activeRequests() {
    // always zero for filters
    return 0;
  }

  @Override
  public String matchPath() {
    return path;
  }

  @Override
  public boolean matches(String requestUri) {
    return matchAll || pathParser.matches(requestUri);
  }

  @Override
  public void handle(Context ctx) {
    handler.handle(ctx);
  }

  @Override
  public SpiRoutes.Params pathParams(String uri) {
    throw new IllegalStateException("not allowed");
  }

  @Override
  public int getSegmentCount() {
    throw new IllegalStateException("not allowed");
  }

  @Override
  public boolean includesWildcard() {
    return pathParser != null && pathParser.includesWildcard();
  }
}
