package io.avaje.jex.routes;

import io.avaje.jex.Context;
import io.avaje.jex.Handler;
import io.avaje.jex.spi.SpiRoutes;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

class RouteEntry implements SpiRoutes.Entry {

  private final AtomicLong active = new AtomicLong();
  private final PathParser path;
  private final Handler handler;

  RouteEntry(PathParser path, Handler handler) {
    this.path = path;
    this.handler = handler;
  }

  @Override
  public void inc() {
    active.incrementAndGet();
  }

  @Override
  public void dec() {
    active.decrementAndGet();
  }

  @Override
  public long activeRequests() {
    return active.get();
  }

  @Override
  public boolean matches(String requestUri) {
    return path.matches(requestUri);
  }

  @Override
  public void handle(Context ctx) {
    handler.handle(ctx);
  }

  @Override
  public Map<String, String> pathParams(String uri) {
    return path.extractPathParams(uri);
  }

  @Override
  public String matchPath() {
    return path.raw();
  }

  @Override
  public int segmentCount() {
    return path.segmentCount();
  }

  @Override
  public boolean multiSlash() {
    return path.multiSlash();
  }

  @Override
  public boolean literal() {
    return path.literal();
  }
}
