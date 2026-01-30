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
    return new Builder();
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

  /** A builder for creating a {@link WebSocketListener} with specific event handlers. */
  final class Builder {
    private Consumer<WsOpen> onOpen;
    private Consumer<WsMessage> onMessage;
    private Consumer<WsBinaryMessage> onBinaryMessage;
    private Consumer<WsClose> onClose;
    private Consumer<WsPong> onPong;
    private Consumer<WsError> onError;

    private Builder() {}

    /**
     * Set the handler for the WebSocket open event.
     *
     * @param handler Consumer for {@link WsOpen}
     * @return this builder
     */
    public Builder onOpen(Consumer<WsOpen> handler) {
      this.onOpen = handler;
      return this;
    }

    /**
     * Set the handler for the WebSocket text message event.
     *
     * @param handler Consumer for {@link WsMessage}
     * @return this builder
     */
    public Builder onMessage(Consumer<WsMessage> handler) {
      this.onMessage = handler;
      return this;
    }

    /**
     * Set the handler for the WebSocket binary message event.
     *
     * @param handler Consumer for {@link WsBinaryMessage}
     * @return this builder
     */
    public Builder onBinaryMessage(Consumer<WsBinaryMessage> handler) {
      this.onBinaryMessage = handler;
      return this;
    }

    /**
     * Set the handler for the WebSocket close event.
     *
     * @param handler Consumer for {@link WsClose}
     * @return this builder
     */
    public Builder onClose(Consumer<WsClose> handler) {
      this.onClose = handler;
      return this;
    }

    /**
     * Set the handler for the WebSocket pong event.
     *
     * @param handler Consumer for {@link WsPong}
     * @return this builder
     */
    public Builder onPong(Consumer<WsPong> handler) {
      this.onPong = handler;
      return this;
    }

    /**
     * Set the handler for the WebSocket error event.
     *
     * @param handler Consumer for {@link WsError}
     * @return this builder
     */
    public Builder onError(Consumer<WsError> handler) {
      this.onError = handler;
      return this;
    }

    /**
     * Build a {@link WebSocketListener} implementation using the configured handlers.
     *
     * @return a new {@link WebSocketListener} instance
     */
    public WebSocketListener build() {
      return new WebSocketListener() {

        @Override
        public void onOpen(WsOpen wsOpen) {
          if (onOpen != null) onOpen.accept(wsOpen);
        }

        @Override
        public void onMessage(WsMessage message) {
          if (onMessage != null) onMessage.accept(message);
        }

        @Override
        public void onBinaryMessage(WsBinaryMessage binaryPayload) {
          if (onBinaryMessage != null) onBinaryMessage.accept(binaryPayload);
        }

        @Override
        public void onClose(WsClose wsClose) {
          if (onClose != null) onClose.accept(wsClose);
        }

        @Override
        public void onPong(WsPong wsPong) {
          if (onPong != null) onPong.accept(wsPong);
        }

        @Override
        public void onError(WsError wsError) {
          if (onError != null) onError.accept(wsError);
        }
      };
    }
  }
}
