package io.avaje.jex.jetty;

import io.avaje.jex.Context;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;
import io.avaje.jex.http.NotFoundResponse;
import io.avaje.jex.spi.SpiContext;
import io.avaje.jex.spi.SpiRoutes;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.util.Map;

class JexHandler extends Handler.Abstract {

  //private static final String X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";
  private final SpiRoutes routes;
  private final ServiceManager manager;
  private final StaticHandler staticHandler;

  JexHandler(Jex jex, SpiRoutes routes, ServiceManager manager, StaticHandler staticHandler) {
    this.routes = routes;
    this.manager = manager;
    this.staticHandler = staticHandler;
  }

  @Override
  public boolean handle(Request req, Response res, Callback callback) throws Exception {

  //@Override
  //public void handle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse res) {
//    try {
      final Routing.Type routeType = method(req);
      HttpURI httpURI = req.getHttpURI();
      String uri = httpURI.getPathQuery();
      SpiRoutes.Entry route = routes.match(routeType, uri);
      if (route == null) {
        var ctx = new JexHttpContext(manager, req, res, uri);
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
        final Map<String, String> params = route.pathParams(uri);
        var ctx = new JexHttpContext(manager, req, res, route.matchPath(), params);
        route.inc();
        try {
          processRoute(ctx, uri, route);
          routes.after(uri, ctx);
          callback.succeeded();
        } catch (Exception e) {
          handleException(ctx, e);
        } finally {
          route.dec();
        }
      }
      return true;
//    }
  }

  private void handleException(SpiContext ctx, Exception e) {
    manager.handleException(ctx, e);
  }

  private void processRoute(JexHttpContext ctx, String uri, SpiRoutes.Entry route) {
    routes.before(uri, ctx);
    ctx.setMode(null);
    route.handle(ctx);
  }

  //String target, Request baseRequest,
  private void processNoRoute(JexHttpContext ctx, String uri, Routing.Type routeType) {
    routes.before(uri, ctx);
    if (routeType == Routing.Type.HEAD && hasGetHandler(uri)) {
      processHead(ctx);
      return;
    }
//    if (routeType == Routing.Type.GET || routeType == Routing.Type.HEAD) {
//      // check if handled by static resource
//      if (staticHandler != null && staticHandler.handle(target, baseRequest, ctx.req(), ctx.res())) {
//        return;
//      }
//      // todo: check if handled by singlePageHandler
//      //if (config.inner.singlePageHandler.handle(ctx)) return@tryWithExceptionMapper
//    }
//    if (routeType == Routing.Type.OPTIONS && isCorsEnabled(config)) { // CORS is enabled, so we return 200 for OPTIONS
//      return@tryWithExceptionMapper
//    }
//    if (prefer405) {
//      //&& availableHandlerTypes.isNotEmpty()
//      //val availableHandlerTypes = MethodNotAllowedUtil.findAvailableHttpHandlerTypes(matcher, requestUri)
//      //throw MethodNotAllowedResponse(details = MethodNotAllowedUtil.getAvailableHandlerTypes(ctx, availableHandlerTypes))
//    }
    throw new NotFoundResponse("uri: " + uri);
  }

  private void processHead(Context ctx) {
    ctx.status(200);
  }

  private boolean hasGetHandler(String uri) {
    return routes.match(Routing.Type.GET, uri) != null;
  }

  private Routing.Type method(Request req) {
    return manager.lookupRoutingType(req.getMethod());
  }
}
