package io.avaje.jex.jdk;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.avaje.jex.ExchangeHandler;
import io.avaje.jex.Routing.Type;
import io.avaje.jex.routes.SpiRoutes;

class BaseHandler implements HttpHandler {

  private final SpiRoutes routes;

  BaseHandler(SpiRoutes routes) {
    this.routes = routes;
  }

  void waitForIdle(long maxSeconds) {
    routes.waitForIdle(maxSeconds);
  }

  @Override
  public void handle(HttpExchange exchange) {

    JdkContext ctx = (JdkContext) exchange.getAttribute("JdkContext");
    ExchangeHandler handlerConsumer =
        (ExchangeHandler) exchange.getAttribute("SpiRoutes.Entry.Handler");

    ctx.setMode(null);
    handlerConsumer.handle(ctx);
    ctx.setMode(Type.FILTER);
  }
}
