package io.avaje.jex.jetty;

import io.avaje.jex.Context;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;
import io.avaje.jex.http.NotFoundResponse;
import io.avaje.jex.spi.SpiContext;
import io.avaje.jex.spi.SpiRoutes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.util.Map;

class JexHandler extends AbstractHandler {

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
  public void handle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse res) {
    try {
      final Routing.Type routeType = method(req);
      final String uri = req.getRequestURI();
      SpiRoutes.Entry route = routes.match(routeType, uri);
      if (route == null) {
        var ctx = new JexHttpContext(manager, req, res, uri);
        routes.inc();
        try {
          processNoRoute(target, baseRequest, ctx, uri, routeType);
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
        } catch (Exception e) {
          handleException(ctx, e);
        } finally {
          route.dec();
        }
      }
    } finally {
      baseRequest.setHandled(true);
    }
  }

  private void handleException(SpiContext ctx, Exception e) {
    manager.handleException(ctx, e);
  }

  private void processRoute(JexHttpContext ctx, String uri, SpiRoutes.Entry route) {
    routes.before(uri, ctx);
    ctx.setMode(null);
    route.handle(ctx);
  }

  private void processNoRoute(String target, Request baseRequest, JexHttpContext ctx, String uri, Routing.Type routeType) {
    routes.before(uri, ctx);
    if (routeType == Routing.Type.HEAD && hasGetHandler(uri)) {
      processHead(ctx);
      return;
    }
    if (routeType == Routing.Type.GET || routeType == Routing.Type.HEAD) {
      // check if handled by static resource
      if (staticHandler != null && staticHandler.handle(target, baseRequest, ctx.req(), ctx.res())) {
        return;
      }
      // todo: check if handled by singlePageHandler
      //if (config.inner.singlePageHandler.handle(ctx)) return@tryWithExceptionMapper
    }
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

  private Routing.Type method(HttpServletRequest req) {
    return manager.lookupRoutingType(req.getMethod());
  }
}
