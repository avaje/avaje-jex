package io.avaje.jex.http3.flupke.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

class FlupkeHttpContext extends HttpContext {

  private final HttpSpiContextHandler jettyContextHandler;
  private final HttpServer server;
  private final Map<String, Object> attributes = new HashMap<>();
  private final List<Filter> filters = new ArrayList<>();
  private Authenticator authenticator;
  private HttpHandler httpHandler;

  protected FlupkeHttpContext(HttpServer server, HttpHandler handler) {
    httpHandler = handler;
    this.server = server;
    jettyContextHandler = new HttpSpiContextHandler(this, handler);
  }

  protected HttpSpiContextHandler flupkeHandler() {
    return jettyContextHandler;
  }

  @Override
  public HttpHandler getHandler() {
    return httpHandler;
  }

  @Override
  public void setHandler(HttpHandler h) {
    jettyContextHandler.setHttpHandler(h);
  }

  @Override
  public String getPath() {
    return "/";
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
