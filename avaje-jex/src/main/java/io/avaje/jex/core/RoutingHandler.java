package io.avaje.jex.core;

import java.util.List;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.avaje.jex.http.HttpFilter;
import io.avaje.jex.http.NotFoundException;
import io.avaje.jex.routes.SpiRoutes;

final class RoutingHandler implements HttpHandler {

  private final SpiRoutes routes;
  private final ServiceManager mgr;
  private final List<HttpFilter> filters;

  RoutingHandler(SpiRoutes routes, ServiceManager mgr) {
    this.mgr = mgr;
    this.routes = routes;
    this.filters = routes.filters();
  }

  void waitForIdle(long maxSeconds) {
    routes.waitForIdle(maxSeconds);
  }

  @Override
  public void handle(HttpExchange exchange) {
    final var uri = exchange.getRequestURI().getPath();
    final var routeType = mgr.lookupRoutingType(exchange.getRequestMethod());
    final var route = routes.match(routeType, uri);

    if (route == null) {
      var ctx = new JdkContext(mgr, exchange, uri, Set.of());
      ctx.setMode(Mode.EXCHANGE);
      mgr.handleException(
        ctx,
        new NotFoundException("No route matching http method %s, with path %s".formatted(routeType.name(), uri)));
    } else {
      route.inc();
      try {
        JdkContext ctx = new JdkContext(mgr, exchange, route, uri);
        CtxHolder.runWith(ctx,
            () -> {
              try {
                ctx.setMode(Mode.BEFORE);
                new BaseFilterChain(filters.iterator(), route.handler(), ctx, mgr).proceed();
                handleNoResponse(ctx);
              } catch (Exception e) {
                mgr.handleException(ctx, e);
              }
            });
      } finally {
        route.dec();
        exchange.close();
      }
    }
  }

  private void handleNoResponse(JdkContext ctx) {
    if (!ctx.responseSent()) {
      final int status = ctx.status();
      ctx.writeEmpty(status == 0 ? 204 : status);
    }
  }
}
