package io.avaje.jex.jdk;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.avaje.jex.Context;
import io.avaje.jex.Routing;
import io.avaje.jex.spi.SpiRoutes;

import java.io.IOException;
import java.net.URI;

class BaseHandler implements HttpHandler {

  final SpiRoutes routes;
  final JdkServiceManager mgr;

  BaseHandler(SpiRoutes routes, JdkServiceManager mgr) {
    this.mgr = mgr;
    this.routes = routes;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {

    final String requestMethod = exchange.getRequestMethod();
    final URI requestURI = exchange.getRequestURI();
    final String fragment = requestURI.getFragment();
    final String uri = requestURI.getPath();
    final Routing.Type type = mgr.lookupRoutingType(requestMethod);
    final SpiRoutes.Entry match = routes.match(type, uri);

    if (match != null) {
      final SpiRoutes.Params params = match.pathParams(uri);
      //SpiContext ctx = new JexHttpContext(manager, req, res, route.matchPath(), params);
      Context ctx = new JdkContext(mgr, exchange, match.matchPath(), params);
      match.handle(ctx);
    }

  }
}
