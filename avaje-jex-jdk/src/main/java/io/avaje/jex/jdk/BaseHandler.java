package io.avaje.jex.jdk;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.avaje.jex.Context;
import io.avaje.jex.Routing;
import io.avaje.jex.http.NotFoundResponse;
import io.avaje.jex.spi.SpiContext;
import io.avaje.jex.spi.SpiRoutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.LockSupport;

class BaseHandler implements HttpHandler {

  private static final Logger log = LoggerFactory.getLogger(BaseHandler.class);

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
        final SpiRoutes.Params params = route.pathParams(uri);
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
