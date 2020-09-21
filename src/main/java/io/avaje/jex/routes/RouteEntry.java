package io.avaje.jex.routes;

import io.avaje.jex.Context;
import io.avaje.jex.Handler;
import io.avaje.jex.Role;
import io.avaje.jex.Routing;
import io.avaje.jex.spi.SpiRoutes;

import java.util.Map;
import java.util.Set;

class RouteEntry implements SpiRoutes.Entry {

  private final PathParser pathParser;
  private final Routing.Type type;
  private final String path;
  private final Handler handler;
  private final Set<Role> roles;

  RouteEntry(PathParser pathParser, Routing.Entry apiEntry) {
    this.pathParser = pathParser;
    this.type = apiEntry.getType();
    this.path = apiEntry.getPath();
    this.handler = apiEntry.getHandler();
    this.roles = apiEntry.getRoles();
  }

  @Override
  public boolean matches(String requestUri) {
    return pathParser.matches(requestUri);
  }

  @Override
  public void handle(Context ctx) {
    handler.handle(ctx);
  }

  @Override
  public Map<String, String> pathParams(String uri) {
    return pathParser.extractPathParams(uri);
  }

  @Override
  public String rawPath() {
    return pathParser.raw();
  }

  @Override
  public int getSegmentCount() {
    return pathParser.getSegmentCount();
  }
}
