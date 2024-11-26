package io.avaje.jex.jdk;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import io.avaje.jex.ExchangeHandler;
import io.avaje.jex.Routing;
import io.avaje.jex.Routing.Type;
import io.avaje.jex.compression.CompressionConfig;
import io.avaje.jex.http.NotFoundException;
import io.avaje.jex.routes.SpiRoutes;
import io.avaje.jex.spi.SpiContext;

final class RoutingFilter extends Filter {

  private final SpiRoutes routes;
  private final CtxServiceManager mgr;
  private final CompressionConfig compressionConfig;

  RoutingFilter(SpiRoutes routes, CtxServiceManager mgr, CompressionConfig compressionConfig) {
    this.mgr = mgr;
    this.routes = routes;
    this.compressionConfig = compressionConfig;
  }

  void waitForIdle(long maxSeconds) {
    routes.waitForIdle(maxSeconds);
  }

  @Override
  public void doFilter(HttpExchange exchange, Filter.Chain chain) {

    final String uri = exchange.getRequestURI().getPath();
    final Routing.Type routeType = mgr.lookupRoutingType(exchange.getRequestMethod());
    final SpiRoutes.Entry route = routes.match(routeType, uri);

    if (route == null) {
      var ctx = new JdkContext(mgr, compressionConfig, exchange, uri, Set.of());
      routes.inc();
      try {
        processNoRoute(ctx, uri, routeType);
        exchange.setAttribute("JdkContext", ctx);
        chain.doFilter(exchange);
        handleNoResponse(exchange);
      } catch (Exception e) {
        handleException(ctx, e);
      } finally {
        routes.dec();
        exchange.close();
      }
    } else {
      route.inc();
      try {
        final Map<String, String> params = route.pathParams(uri);
        JdkContext ctx =
            new JdkContext(
                mgr, compressionConfig, exchange, route.matchPath(), params, route.roles());
        try {
          ctx.setMode(Type.FILTER);
          exchange.setAttribute("JdkContext", ctx);
          ExchangeHandler handlerConsumer = route::handle;
          exchange.setAttribute("SpiRoutes.Entry.Handler", handlerConsumer);
          chain.doFilter(exchange);
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

  private void handleException(SpiContext ctx, Exception e) {
    mgr.handleException(ctx, e);
  }

  private void processNoRoute(JdkContext ctx, String uri, Routing.Type routeType) {
    if (routeType == Routing.Type.HEAD && hasGetHandler(uri)) {
      ctx.status(200);
      return;
    }
    throw new NotFoundException("uri: " + uri);
  }

  private boolean hasGetHandler(String uri) {
    return routes.match(Routing.Type.GET, uri) != null;
  }

  @Override
  public String description() {
    return "Routing Filter";
  }
}