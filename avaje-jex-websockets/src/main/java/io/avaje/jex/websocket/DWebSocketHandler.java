package io.avaje.jex.websocket;

import io.avaje.jex.http.Context;
import io.avaje.jex.websocket.internal.WebSocketHandler;

class DWebSocketHandler extends WebSocketHandler {

  private final WebSocketListener listener;

  DWebSocketHandler(WebSocketListener listener) {
    this.listener = listener;
  }

  @Override
  protected DWebSocket openWebSocket(Context exchange) {

    return new DWebSocket(exchange, listener);
  }
}
