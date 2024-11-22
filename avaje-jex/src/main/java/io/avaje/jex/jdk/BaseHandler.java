package io.avaje.jex.jdk;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.avaje.jex.Routing;
import io.avaje.jex.http.HttpResponseException;
import io.avaje.jex.routes.SpiRoutes;
import io.avaje.jex.spi.SpiContext;

import java.util.Map;

class BaseHandler implements HttpHandler {

  private final SpiRoutes routes;
  private final ServiceManager mgr;

  BaseHandler(SpiRoutes routes, ServiceManager mgr) {
    this.mgr = mgr;
    this.routes = routes;
  }

  void waitForIdle(long maxSeconds) {
    routes.waitForIdle(maxSeconds);
  }

  @Override
  public void handle(HttpExchange exchange) {

    final String uri = exchange.getRequestURI().getPath();
    final Routing.Type routeType = mgr.lookupRoutingType(exchange.getRequestMethod());
    final SpiRoutes.Entry route = routes.match(routeType, uri);

    if (route == null) {
      var ctx = new JdkContext(mgr, exchange, uri);
      routes.inc();
      try {
        processNoRoute(ctx, uri, routeType);
        routes.after(uri, ctx);
      } catch (Exception e) {
        handleException(ctx, e);
      } finally {
        routes.dec();
      }
    } else {
      route.inc();
      try {
        final Map<String, String> params = route.pathParams(uri);
        JdkContext ctx = new JdkContext(mgr, exchange, route.matchPath(), params);
        try {
          processRoute(ctx, uri, route);
          routes.after(uri, ctx);
        } catch (Exception e) {
          handleException(ctx, e);
        }
      } finally {
        route.dec();
      }
    }
  }

  private void handleException(SpiContext ctx, Exception e) {
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
      ctx.status(200);
      return;
    }
    throw new HttpResponseException(404, "uri: " + uri);
  }

  private boolean hasGetHandler(String uri) {
    return routes.match(Routing.Type.GET, uri) != null;
  }

}
