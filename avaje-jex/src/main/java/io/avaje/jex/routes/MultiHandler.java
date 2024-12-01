package io.avaje.jex.routes;

import io.avaje.jex.Context;
import io.avaje.jex.ExchangeHandler;

import java.io.IOException;

final class MultiHandler implements ExchangeHandler {

  private final ExchangeHandler[] handlers;

  MultiHandler(ExchangeHandler[] handlers) {
    this.handlers = handlers;
  }

  @Override
  public void handle(Context ctx) throws IOException {
    for (ExchangeHandler handler : handlers) {
      handler.handle(ctx);
      if (ctx.responseSent()) {
        break;
      }
    }
  }
}
