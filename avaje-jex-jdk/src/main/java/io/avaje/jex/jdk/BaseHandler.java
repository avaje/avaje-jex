package io.avaje.jex.jdk;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.avaje.jex.Context;
import io.avaje.jex.Routing;
import io.avaje.jex.http.NotFoundResponse;
import io.avaje.jex.spi.SpiRoutes;

import java.io.IOException;
import java.net.URI;

class BaseHandler implements HttpHandler {

  final SpiRoutes routes;
  final ServiceManager mgr;

  BaseHandler(SpiRoutes routes, ServiceManager mgr) {
    this.mgr = mgr;
    this.routes = routes;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {

    final String requestMethod = exchange.getRequestMethod();
    final URI requestURI = exchange.getRequestURI();
    final String uri = requestURI.getPath();
    final Routing.Type routeType = mgr.lookupRoutingType(requestMethod);
    final SpiRoutes.Entry route = routes.match(routeType, uri);

    if (route == null) {
      var ctx = new JdkContext(mgr, exchange, uri);
      try {
        processNoRoute(ctx, uri, routeType);
        routes.after(uri, ctx);
      } catch (Exception e) {
        handleException(ctx, e);
      }
    } else {
      final SpiRoutes.Params params = route.pathParams(uri);
      JdkContext ctx = new JdkContext(mgr, exchange, route.matchPath(), params);
      try {
        processRoute(ctx, uri, route);
        routes.after(uri, ctx);
      } catch (Exception e) {
        handleException(ctx, e);
      }
    }
  }

  private void handleException(Context ctx, Exception e) {
    mgr.handleException(ctx, e);
  }

  private void processRoute(JdkContext ctx, String uri, SpiRoutes.Entry route) {
    routes.before(uri, ctx);
    ctx.setMode(null);
    route.handle(ctx);
  }

  private void processNoRoute(JdkContext ctx, String uri, Routing.Type routeType) {
    routes.before(uri, ctx);
    if (routeType == Routing.Type.HEAD && hasGetHandler(uri)) {
      processHead(ctx);
      return;
    }
//    if (routeType == Routing.Type.GET || routeType == Routing.Type.HEAD) {
//      // check if handled by static resource
//      // check if handled by singlePageHandler
//    }
    throw new NotFoundResponse("uri: " + uri);
  }

  private void processHead(Context ctx) {
    ctx.status(200);
  }

  private boolean hasGetHandler(String uri) {
    return routes.match(Routing.Type.GET, uri) != null;
  }

}
