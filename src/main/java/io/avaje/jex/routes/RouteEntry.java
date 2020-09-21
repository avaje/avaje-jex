package io.avaje.jex.routes;

import io.avaje.jex.Context;
import io.avaje.jex.Handler;
import io.avaje.jex.Role;
import io.avaje.jex.Routing;
import io.avaje.jex.spi.SpiRoutes;

import java.util.Map;
import java.util.Set;

class RouteEntry implements SpiRoutes.Entry {

  private final PathParser path;
  private final Handler handler;
  private final Set<Role> roles;

  RouteEntry(PathParser path, Routing.Entry apiEntry) {
    this.path = path;
    this.handler = apiEntry.getHandler();
    this.roles = apiEntry.getRoles();
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
  public int getSegmentCount() {
    return path.getSegmentCount();
  }
}
