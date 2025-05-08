package io.avaje.jex.grizzly.spi;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class GrizzlyHttpContext extends com.sun.net.httpserver.HttpContext {

  private final GrizzlyHandler grizzlyHandler;
  private final HttpServer server;

  private final Map<String, Object> attributes = new HashMap<>();

  private final List<Filter> filters = new ArrayList<>();

  private Authenticator authenticator;

  private String contextPath;

  protected GrizzlyHttpContext(HttpServer server, String contextPath, HttpHandler handler) {
    this.server = server;
    this.grizzlyHandler = new GrizzlyHandler(this, handler);
    this.contextPath = contextPath;
  }

  GrizzlyHandler getGrizzlyHandler() {
    return grizzlyHandler;
  }

  @Override
  public HttpHandler getHandler() {
    return grizzlyHandler.getHttpHandler();
  }

  @Override
  public void setHandler(HttpHandler h) {
    grizzlyHandler.setHttpHandler(h);
  }

  @Override
  public String getPath() {
    return contextPath;
  }

  @Override
  public HttpServer getServer() {
    return server;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public List<Filter> getFilters() {
    return filters;
  }

  @Override
  public Authenticator setAuthenticator(Authenticator auth) {
    Authenticator previous = authenticator;
    authenticator = auth;
    return previous;
  }

  @Override
  public Authenticator getAuthenticator() {
    return authenticator;
  }
}
