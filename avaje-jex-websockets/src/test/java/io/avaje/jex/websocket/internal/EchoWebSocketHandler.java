package io.avaje.jex.websocket.internal;

import io.avaje.jex.websocket.WebSocketListener;
import io.avaje.jex.websocket.WsContext.WsMessage;

public class EchoWebSocketHandler implements WebSocketListener {

  private StringBuilder sb = new StringBuilder();

  @Override
  public void onMessage(WsMessage message) {
    sb.append(message.message());
    if (message.wsFrame().isFin()) {
      String msg = sb.toString();
      sb = new StringBuilder();
      message.send(msg);
    }
    message.closeSession();
  }
}
