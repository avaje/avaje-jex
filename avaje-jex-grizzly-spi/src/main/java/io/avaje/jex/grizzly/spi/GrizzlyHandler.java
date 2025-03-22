package io.avaje.jex.grizzly.spi;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import com.sun.net.httpserver.Filter.Chain;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

final class GrizzlyHandler extends org.glassfish.grizzly.http.server.HttpHandler {

  private final HttpContext httpContext;

  private HttpHandler handler;

  GrizzlyHandler(HttpContext httpContext, HttpHandler httpHandler) {
    super(httpContext.getPath());
    this.httpContext = httpContext;
    this.handler = httpHandler;
  }

  @Override
  public void service(Request request, Response response) {

    try (HttpExchange exchange =
        request.isSecure()
            ? new GrizzlyHttpsExchange(httpContext, request, response)
            : new GrizzlyHttpExchange(httpContext, request, response)) {

      new Chain(httpContext.getFilters(), handler).doFilter(exchange);

    } catch (IOException ex) {
      throw new UncheckedIOException(null);
    }
  }

  public HttpHandler getHttpHandler() {
    return handler;
  }

  public void setHttpHandler(HttpHandler handler) {
    this.handler = handler;
  }
}
