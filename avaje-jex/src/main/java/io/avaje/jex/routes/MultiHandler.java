package io.avaje.jex.routes;

import io.avaje.jex.http.Context;
import io.avaje.jex.http.ExchangeHandler;

final class MultiHandler implements ExchangeHandler {

  private final ExchangeHandler[] handlers;

  MultiHandler(ExchangeHandler[] handlers) {
    this.handlers = handlers;
  }

  @Override
  public void handle(Context ctx) throws Exception {
    for (ExchangeHandler handler : handlers) {
      handler.handle(ctx);
      if (ctx.responseSent()) {
        break;
      }
    }
  }
}
