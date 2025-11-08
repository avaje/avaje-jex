package io.avaje.jex.http3.flupke.core;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import com.sun.net.httpserver.Filter.Chain;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;

import io.avaje.applog.AppLog;
import tech.kwik.flupke.server.HttpRequestHandler;
import tech.kwik.flupke.server.HttpServerRequest;
import tech.kwik.flupke.server.HttpServerResponse;

class HttpSpiContextHandler implements HttpRequestHandler {
  public static final Logger LOG = AppLog.getLogger(HttpSpiContextHandler.class);

  private final HttpContext httpContext;

  private HttpHandler httpHandler;

  public HttpSpiContextHandler(HttpContext httpContext, HttpHandler httpHandler) {
    this.httpContext = httpContext;
    this.httpHandler = httpHandler;
  }

  @Override
  public void handleRequest(HttpServerRequest request, HttpServerResponse response)
      throws IOException {
    var exchange = new FlupkeExchange(request, response, httpContext);
    try {
      new Chain(httpContext.getFilters(), httpHandler).doFilter(exchange);
    } catch (Exception ex) {
      LOG.log(Level.ERROR, "Failed to handle", ex);
      response.setStatus(500);
    } finally {
      exchange.close();
    }
  }

  void setHttpHandler(HttpHandler httpHandler) {
    this.httpHandler = httpHandler;
  }
}
