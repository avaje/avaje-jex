package io.avaje.jex.websocket;

import java.util.function.Consumer;

import io.avaje.jex.websocket.WsContext.WsBinaryMessage;
import io.avaje.jex.websocket.WsContext.WsClose;
import io.avaje.jex.websocket.WsContext.WsError;
import io.avaje.jex.websocket.WsContext.WsMessage;
import io.avaje.jex.websocket.WsContext.WsOpen;
import io.avaje.jex.websocket.WsContext.WsPong;

/** A builder for creating a {@link WebSocketListener} with specific event handlers. */
class ListenerBuilder implements WebSocketListener.Builder {
  private Consumer<WsOpen> onOpen;
  private Consumer<WsMessage> onMessage;
  private Consumer<WsBinaryMessage> onBinaryMessage;
  private Consumer<WsClose> onClose;
  private Consumer<WsPong> onPong;
  private Consumer<WsError> onError;

  /**
   * Set the handler for the WebSocket open event.
   *
   * @param handler Consumer for {@link WsOpen}
   * @return this builder
   */
  @Override
  public ListenerBuilder onOpen(Consumer<WsOpen> handler) {
    this.onOpen = handler;
    return this;
  }

  /**
   * Set the handler for the WebSocket text message event.
   *
   * @param handler Consumer for {@link WsMessage}
   * @return this builder
   */
  @Override
  public ListenerBuilder onMessage(Consumer<WsMessage> handler) {
    this.onMessage = handler;
    return this;
  }

  /**
   * Set the handler for the WebSocket binary message event.
   *
   * @param handler Consumer for {@link WsBinaryMessage}
   * @return this builder
   */
  @Override
  public ListenerBuilder onBinaryMessage(Consumer<WsBinaryMessage> handler) {
    this.onBinaryMessage = handler;
    return this;
  }

  /**
   * Set the handler for the WebSocket close event.
   *
   * @param handler Consumer for {@link WsClose}
   * @return this builder
   */
  @Override
  public ListenerBuilder onClose(Consumer<WsClose> handler) {
    this.onClose = handler;
    return this;
  }

  /**
   * Set the handler for the WebSocket pong event.
   *
   * @param handler Consumer for {@link WsPong}
   * @return this builder
   */
  @Override
  public ListenerBuilder onPong(Consumer<WsPong> handler) {
    this.onPong = handler;
    return this;
  }

  /**
   * Set the handler for the WebSocket error event.
   *
   * @param handler Consumer for {@link WsError}
   * @return this builder
   */
  @Override
  public ListenerBuilder onError(Consumer<WsError> handler) {
    this.onError = handler;
    return this;
  }

  /**
   * Build a {@link WebSocketListener} implementation using the configured handlers.
   *
   * @return a new {@link WebSocketListener} instance
   */
  @Override
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
