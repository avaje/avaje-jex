package io.avaje.jex.routes;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import io.avaje.jex.Context;
import io.avaje.jex.ExchangeHandler;
import io.avaje.jex.security.Role;

class RouteEntry implements SpiRoutes.Entry {

  private final AtomicLong active = new AtomicLong();
  private final PathParser path;
  private final ExchangeHandler handler;
  private final Set<Role> roles;

  RouteEntry(PathParser path, ExchangeHandler handler, Set<Role> roles) {
    this.path = path;
    this.handler = handler;
    this.roles = roles;
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

  @Override
  public Set<Role> roles() {
    return roles;
  }
}
