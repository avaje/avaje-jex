package io.avaje.jex.grizzly;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;
import io.avaje.jex.http.NotFoundResponse;
import io.avaje.jex.spi.SpiContext;
import io.avaje.jex.spi.SpiRoutes;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import java.util.Map;

class RouteHandler extends HttpHandler {

  private final SpiRoutes routes;
  private final ServiceManager mgr;

  RouteHandler(SpiRoutes routes, ServiceManager mgr) {
    this.mgr = mgr;
    this.routes = routes;
  }

  @Override
  public void service(Request request, Response response) {

    final String uri = request.getRequestURI();
    final Routing.Type routeType = mgr.lookupRoutingType(request.getMethod().getMethodString());
    final SpiRoutes.Entry route = routes.match(routeType, uri);

    if (route == null) {
      var ctx = new GrizzlyContext(mgr, request, response, uri);
      try {
        processNoRoute(ctx, uri, routeType);
        routes.after(uri, ctx);
      } catch (Exception e) {
        handleException(ctx, e);
      }
    } else {
      final Map<String, String> params = route.pathParams(uri);
      var ctx = new GrizzlyContext(mgr, request, response, route.matchPath(), params);
      try {
        processRoute(ctx, uri, route);
        routes.after(uri, ctx);
      } catch (Exception e) {
        handleException(ctx, e);
      }
    }
  }

  private void handleException(SpiContext ctx, Exception e) {
    mgr.handleException(ctx, e);
  }

  private void processRoute(GrizzlyContext ctx, String uri, SpiRoutes.Entry route) {
    routes.before(uri, ctx);
    ctx.setMode(null);
    route.handle(ctx);
  }

  private void processNoRoute(GrizzlyContext ctx, String uri, Routing.Type routeType) {
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
