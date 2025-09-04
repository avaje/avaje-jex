package io.avaje.jex.websocket;

import io.avaje.jex.http.Context;
import io.avaje.jex.websocket.WsContext.WsBinaryMessage;
import io.avaje.jex.websocket.WsContext.WsClose;
import io.avaje.jex.websocket.WsContext.WsError;
import io.avaje.jex.websocket.WsContext.WsMessage;
import io.avaje.jex.websocket.WsContext.WsOpen;
import io.avaje.jex.websocket.WsContext.WsPong;
import io.avaje.jex.websocket.exception.CloseCode;
import io.avaje.jex.websocket.internal.AbstractWebSocket;

class DWebSocket extends AbstractWebSocket {

  private final WebSocketListener listener;
  private final Context ctx;

  DWebSocket(Context ctx, WebSocketListener listener) {
    super(ctx.exchange());
    this.listener = listener;
    this.ctx = ctx;
  }

  @Override
  protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
    listener.onClose(new WsClose(ctx, this, code, reason, initiatedByRemote));
  }

  @Override
  protected void onMessage(WebSocketFrame frame) {
    switch (frame.opCode()) {
      case TEXT -> listener.onMessage(new WsMessage(ctx, this, frame, frame.textPayload()));
      case BINARY ->
          listener.onBinaryMessage(new WsBinaryMessage(ctx, this, frame, frame.binaryPayload()));
      default -> throw new IllegalArgumentException("Unexpected value: ");
    }
  }

  @Override
  protected void onPong(WebSocketFrame pong) {
    listener.onPong(new WsPong(ctx, this, pong));
  }

  @Override
  protected void onOpen() {
    listener.onOpen(new WsOpen(ctx, this));
  }

  @Override
  protected void onError(Exception exception) {
    listener.onError(new WsError(ctx, this, exception));
  }
}
