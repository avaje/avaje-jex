package io.avaje.jex.websocket;

import io.avaje.jex.websocket.WsContext.WsBinaryMessage;
import io.avaje.jex.websocket.WsContext.WsClose;
import io.avaje.jex.websocket.WsContext.WsError;
import io.avaje.jex.websocket.WsContext.WsMessage;
import io.avaje.jex.websocket.WsContext.WsOpen;
import io.avaje.jex.websocket.WsContext.WsPong;

/**
 * Holds the different WebSocket handlers for a specific {@link WsHandlerEntry} or the WebSocket
 * log.
 */
public interface WebSocketListener {
  /**
   * Called when a binary message is received.
   *
   * @param binaryPayload the binary payload
   */
  default void onBinaryMessage(WsBinaryMessage binaryPayload) {}

  /**
   * Called when the websocket is closed.
   *
   * @param wsClose the close context
   */
  default void onClose(WsClose wsClose) {}

  /**
   * Called when a text message is received.
   *
   * @param message the text message
   */
  default void onMessage(WsMessage message) {}

  /**
   * Called when the websocket is opened.
   *
   * @param wsOpenContext the open context
   */
  default void onOpen(WsOpen wsOpen) {}

  /**
   * Called when a pong is received.
   *
   * @param pong the pong
   */
  default void onPong(WsPong wsPong) {}

  /**
   * Called when an error occurs.
   *
   * @param wsError the error
   */
  default void onError(WsError wsError) {}
}
