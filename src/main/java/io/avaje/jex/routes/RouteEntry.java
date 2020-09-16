package io.avaje.jex.routes;

import io.avaje.jex.Context;
import io.avaje.jex.Handler;
import io.avaje.jex.Role;

import java.util.Map;
import java.util.Set;

public class RouteEntry {

  private final PathParser pathParser;
  private final HandlerType type;
  private final String path;
  private final Handler handler;
  private final Set<Role> roles;

  public RouteEntry(PathParser pathParser, WebApiEntry apiEntry) {
    this.pathParser = pathParser;
    this.type = apiEntry.getType();
    this.path = apiEntry.getPath();
    this.handler = apiEntry.getHandler();
    this.roles = apiEntry.getRoles();
  }

  public boolean matches(String requestUri) {
    return pathParser.matches(requestUri);
  }

  public void handle(Context ctx) {
    handler.handle(ctx);
  }

  public Map<String, String> pathParams(String uri) {
    return pathParser.extractPathParams(uri);
  }

  public String rawPath() {
    return pathParser.raw();
  }

  public int getSegmentCount() {
    return pathParser.getSegmentCount();
  }
}
