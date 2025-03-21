package io.avaje.helidon.http.spi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Authenticator.Result;
import com.sun.net.httpserver.Filter.Chain;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpPrincipal;

class GrizzlyHandler extends org.glassfish.grizzly.http.server.HttpHandler {

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
            ? new JettyHttpsExchange(httpContext, request, response)
            : new JettyHttpExchange(httpContext, request, response)) {
      Authenticator auth = httpContext.getAuthenticator();

      if (auth != null && handleAuthentication(request, response, exchange, auth)) {
        return;
      }
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

  private boolean handleAuthentication(
      Request request, Response response, HttpExchange httpExchange, Authenticator auth)
      throws IOException {
    Result result = auth.authenticate(httpExchange);
    if (result instanceof Authenticator.Failure fail) {
      response.sendError(fail.getResponseCode(), "");
      for (Map.Entry<String, List<String>> header : httpExchange.getResponseHeaders().entrySet()) {
        for (String value : header.getValue()) response.addHeader(header.getKey(), value);
      }
      return true;
    }

    if (result instanceof Authenticator.Retry ry) {
      for (Map.Entry<String, List<String>> header : httpExchange.getResponseHeaders().entrySet()) {
        for (String value : header.getValue()) {
          response.addHeader(header.getKey(), value);
        }
      }
      response.sendError(ry.getResponseCode(), "Failed to Authenticate");
      return true;
    }

    if (result instanceof Authenticator.Success s) {
      HttpPrincipal principal = s.getPrincipal();
      ((JettyExchange) httpExchange).setPrincipal(principal);
      return false;
    }
    return true;
  }
}
