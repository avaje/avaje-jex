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

  private final HttpServer _server;

  private final Map<String, Object> _attributes = new HashMap<>();

  private final List<Filter> _filters = new ArrayList<>();

  private Authenticator _authenticator;

  private HttpHandler httpHandler;

  protected FlupkeHttpContext(HttpServer server, String contextPath, HttpHandler handler) {
    httpHandler = handler;
    this._server = server;
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
    return _server;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return _attributes;
  }

  @Override
  public List<Filter> getFilters() {
    return _filters;
  }

  @Override
  public Authenticator setAuthenticator(Authenticator auth) {
    Authenticator previous = _authenticator;
    _authenticator = auth;
    return previous;
  }

  @Override
  public Authenticator getAuthenticator() {
    return _authenticator;
  }
}
