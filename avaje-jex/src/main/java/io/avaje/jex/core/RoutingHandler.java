package io.avaje.jex.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.avaje.jex.HttpFilter;
import io.avaje.jex.Routing;
import io.avaje.jex.compression.CompressionConfig;
import io.avaje.jex.http.NotFoundException;
import io.avaje.jex.routes.SpiRoutes;

final class RoutingHandler implements HttpHandler {

  private final SpiRoutes routes;
  private final CtxServiceManager mgr;
  private final CompressionConfig compressionConfig;
  private final List<HttpFilter> filters;

  RoutingHandler(SpiRoutes routes, CtxServiceManager mgr, CompressionConfig compressionConfig) {
    this.mgr = mgr;
    this.routes = routes;
    this.filters = routes.filters();
    this.compressionConfig = compressionConfig;
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
      var ctx = new JdkContext(mgr, compressionConfig, exchange, uri, Set.of());
      handleException(ctx, new NotFoundException("uri: " + uri));
    } else {
      route.inc();
      try {
        final Map<String, String> params = route.pathParams(uri);
        JdkContext ctx =
            new JdkContext(
                mgr, compressionConfig, exchange, route.matchPath(), params, route.roles());
        try {
          ctx.setMode(Mode.BEFORE);
          new BaseFilterChain(filters, route.handler(), ctx).proceed();
          handleNoResponse(exchange);
        } catch (Exception e) {
          handleException(ctx, e);
        }
      } finally {
        route.dec();
        exchange.close();
      }
    }
  }

  private void handleNoResponse(HttpExchange exchange) throws IOException {
    if (exchange.getResponseCode() == -1) {
      exchange.sendResponseHeaders(204, -1);
    }
  }

  private void handleException(JdkContext ctx, Exception e) {
    mgr.handleException(ctx, e);
  }
}
