package io.avaje.jex.websocket;

import java.lang.reflect.Type;

import io.avaje.jex.http.Context;
import io.avaje.jex.websocket.exception.CloseCode;

/**
 * The abstract sealed base class that provides the context for a specific WebSocket event.
 *
 * <p>This class encapsulates the underlying {@link Context} (request context) and the {@link
 * WebSocket} connection, offering methods for sending messages and controlling the session.
 * Subclasses represent specific WebSocket events (e.g., Open, Message, Close, Error).
 */
public abstract sealed class WsContext {

  protected final Context ctx;
  private final WebSocket ws;

  protected WsContext(Context ctx, WebSocket ws) {
    this.ctx = ctx;
    this.ws = ws;
  }

  /**
   * Serializes an object to a JSON string using the registered {@code JsonService} and sends it as
   * a text frame over the socket.
   *
   * @param message The object to be serialized and sent.
   */
  public void send(Object message) {
    send(ctx.jsonService().toJsonString(message));
  }

  /**
   * Sends a {@code String} message (Text Frame) over the socket.
   *
   * @param message The string message to send.
   */
  public void send(String message) {
    ws.send(message);
  }

  /**
   * Sends a {@code byte[]} message (Binary Frame) over the socket.
   *
   * @param message The binary data to send.
   */
  public void send(byte[] message) {
    ws.send(message);
  }

  /** Sends a Ping control frame over the socket */
  public void sendPing() {
    sendPing(null);
  }

  /**
   * Sends a Ping control frame over the socket.
   *
   * @param applicationData Optional application data to include in the Ping frame.
   */
  public void sendPing(byte[] applicationData) {
    ws.ping(applicationData != null ? applicationData : new byte[0]);
  }

  /**
   * Returns the underlying HTTP request {@code Context}. This provides access to request headers,
   * path parameters, and attributes.
   *
   * @return The request {@code Context}.
   */
  public Context ctx() {
    return ctx;
  }

  /**
   * Returns the underlying {@code WebSocket} session object.
   *
   * @return The {@code WebSocket} session object.
   */
  public WebSocket ws() {
    return ws;
  }

  /** Closes the WebSocket session gracefully with a default reason. */
  public void closeSession() {
    ws.close(CloseCode.NORMAL_CLOSURE, "Normally closed", false);
  }

  /**
   * Closes the WebSocket session with a specified {@link CloseCode} and an empty reason string.
   *
   * @param code The {@link CloseCode} to send.
   */
  public void closeSession(CloseCode code) {
    ws.close(code, "", false);
  }

  /**
   * Closes the WebSocket session with a specified {@link CloseCode} and a descriptive reason.
   *
   * @param code The {@link CloseCode} to send.
   * @param reason A descriptive string explaining why the session is being closed.
   */
  public void closeSession(CloseCode code, String reason) {
    ws.close(code, reason, false);
  }

  /**
   * Represents the context for an open event. This event occurs when a new connection is
   * established and the handshake is complete.
   */
  public static final class WsOpen extends WsContext {
    WsOpen(Context ctx, WebSocket ws) {
      super(ctx, ws);
    }
  }

  /**
   * Represents the context for a Pong control frame received from the remote endpoint. Pongs are
   * typically received in response to a Ping sent by this endpoint.
   */
  public static final class WsPong extends WsMessageCtx {
    WsPong(Context ctx, WebSocket ws, WebSocketFrame wsFrame) {
      super(ctx, ws, wsFrame);
    }
  }

  /**
   * Represents the context for an error event. This is triggered when an unhandled exception occurs
   * during the lifecycle of the connection.
   */
  public static final class WsError extends WsContext {
    private final Exception error;

    WsError(Context ctx, WebSocket ws, Exception error) {
      super(ctx, ws);
      this.error = error;
    }

    /**
     * Gets the {@code Exception} that caused the error event.
     *
     * @return The underlying {@code Exception}.
     */
    public Exception error() {
      return error;
    }
  }

  /**
   * Represents the context for a close event. This event is triggered when the connection is
   * closed, either locally or by the remote endpoint.
   */
  public static final class WsClose extends WsContext {
    private final CloseCode closeCode;
    private final String reason;
    private final boolean initiatedByRemote;

    WsClose(
        Context ctx, WebSocket ws, CloseCode closeCode, String reason, boolean initiatedByRemote) {
      super(ctx, ws);
      this.closeCode = closeCode;
      this.reason = reason;
      this.initiatedByRemote = initiatedByRemote;
    }

    /**
     * Gets the {@link CloseCode} provided when the connection was closed.
     *
     * @return The {@link CloseCode} indicating the reason for closure.
     */
    public CloseCode closeCode() {
      return closeCode;
    }

    /**
     * Gets the descriptive reason string for the close event, as provided by the closing endpoint.
     *
     * @return The reason string.
     */
    public String reason() {
      return reason;
    }

    /**
     * Indicates whether the close handshake was initiated by the remote endpoint (true) or by the
     * local endpoint (false).
     *
     * @return {@code true} if closed by the remote; {@code false} if closed locally.
     */
    public boolean initiatedByRemote() {
      return initiatedByRemote;
    }
  }

  /**
   * The abstract sealed base class for WebSocket contexts that involve receiving a data frame
   * (e.g., Text, Binary, Pong).
   */
  public abstract static sealed class WsMessageCtx extends WsContext {
    private final WebSocketFrame wsFrame;

    WsMessageCtx(Context ctx, WebSocket ws, WebSocketFrame wsFrame) {
      super(ctx, ws);
      this.wsFrame = wsFrame;
    }

    /**
     * Gets the underlying raw {@code WebSocketFrame}. This is useful for inspecting frame metadata
     * like opcode, RSV bits, etc.
     *
     * @return The raw {@code WebSocketFrame}.
     */
    public WebSocketFrame wsFrame() {
      return wsFrame;
    }

    /**
     * Indicates if this frame is the final fragment of a fragmented message.
     *
     * @return {@code true} if this is the final fragment (FIN bit is set), {@code false} otherwise.
     */
    public boolean isFin() {
      return wsFrame.isFin();
    }
  }

  /** Represents the context for a binary message received from the remote endpoint. */
  public static final class WsBinaryMessage extends WsMessageCtx {
    private final byte[] data;

    WsBinaryMessage(Context ctx, WebSocket ws, WebSocketFrame wsFrame, byte[] data) {
      super(ctx, ws, wsFrame);
      this.data = data;
    }

    /**
     * Gets the raw binary data (payload) of the message.
     *
     * @return The message content as a byte array.
     */
    public byte[] data() {
      return data;
    }
  }

  /** Represents the context for a text message received from the remote endpoint. */
  public static final class WsMessage extends WsMessageCtx {
    private final String message;

    WsMessage(Context ctx, WebSocket ws, WebSocketFrame frame, String message) {
      super(ctx, ws, frame);
      this.message = message;
    }

    /**
     * Gets the text message received from the client.
     *
     * @return The message content as a {@code String}.
     */
    public String message() {
      return message;
    }

    /**
     * Deserializes the received JSON string message into an object of the specified {@code Type}.
     * This uses the application's registered {@code JsonService}.
     *
     * @param <T> The target type.
     * @param type The {@code Type} (e.g., a generic type) to deserialize the message into.
     * @return The deserialized object.
     */
    public <T> T messageAsClass(Type type) {
      return ctx.jsonService().fromJson(type, message);
    }

    /**
     * Deserializes the received JSON string message into an object of the specified {@code Class}.
     *
     * @param <T> The target class type.
     * @param clazz The {@code Class} to deserialize the message into.
     * @return The deserialized object.
     * @see #messageAsClass(Type)
     */
    public <T> T messageAsClass(Class<T> clazz) {
      return messageAsClass((Type) clazz);
    }
  }
}
