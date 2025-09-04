package io.avaje.jex.websocket;

import java.util.function.Consumer;

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
   * Create a builder for a WebSocketListener.
   *
   * @return the builder
   */
  static Builder builder() {
    return new ListenerBuilder();
  }

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

  interface Builder {

    /**
     * Set the handler for the WebSocket open event.
     *
     * @param handler Consumer for {@link WsOpen}
     * @return this builder
     */
    Builder onOpen(Consumer<WsOpen> handler);

    /**
     * Set the handler for the WebSocket text message event.
     *
     * @param handler Consumer for {@link WsMessage}
     * @return this builder
     */
    Builder onMessage(Consumer<WsMessage> handler);

    /**
     * Set the handler for the WebSocket binary message event.
     *
     * @param handler Consumer for {@link WsBinaryMessage}
     * @return this builder
     */
    Builder onBinaryMessage(Consumer<WsBinaryMessage> handler);

    /**
     * Set the handler for the WebSocket close event.
     *
     * @param handler Consumer for {@link WsClose}
     * @return this builder
     */
    Builder onClose(Consumer<WsClose> handler);

    /**
     * Set the handler for the WebSocket pong event.
     *
     * @param handler Consumer for {@link WsPong}
     * @return this builder
     */
    Builder onPong(Consumer<WsPong> handler);

    /**
     * Set the handler for the WebSocket error event.
     *
     * @param handler Consumer for {@link WsError}
     * @return this builder
     */
    Builder onError(Consumer<WsError> handler);

    /** Build the WebSocketListener. */
    WebSocketListener build();
  }
}
