package io.avaje.jex.websocket;

import io.avaje.jex.http.Context;
import io.avaje.jex.websocket.internal.WebSocketHandler;

class WebSocketExchangeHandler extends WebSocketHandler {

  private final WebSocketListener listener;

  WebSocketExchangeHandler(WebSocketListener listener) {
    this.listener = listener;
  }

  @Override
  protected JexWebSocket openWebSocket(Context exchange) {

    return new JexWebSocket(exchange, listener);
  }
}
