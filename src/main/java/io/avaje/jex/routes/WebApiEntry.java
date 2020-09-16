package io.avaje.jex.routes;

import io.avaje.jex.Handler;
import io.avaje.jex.Role;

import java.util.Set;

class WebApiEntry {

  private final HandlerType type;
  private final String path;
  private final Handler handler;
  private final Set<Role> roles;

  WebApiEntry(HandlerType type, String path, Handler handler, Set<Role> roles) {
    this.type = type;
    this.path = path;
    this.handler = handler;
    this.roles = roles;
  }

  WebApiEntry(String path, Handler handler) {
    this.path = path;
    this.handler = handler;
    this.type = null;
    this.roles = null;
  }

  public HandlerType getType() {
    return type;
  }

  public String getPath() {
    return path;
  }

  public Handler getHandler() {
    return handler;
  }

  public Set<Role> getRoles() {
    return roles;
  }

  public boolean matches(String pathInfo) {
    return false;
  }
}
