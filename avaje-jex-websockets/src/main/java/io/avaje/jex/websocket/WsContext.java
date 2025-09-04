package io.avaje.jex.websocket;

import java.lang.reflect.Type;

import io.avaje.jex.http.Context;
import io.avaje.jex.websocket.exception.CloseCode;

/** The context for a WebSocket event */
public abstract sealed class WsContext {

  protected final Context ctx;
  private final WebSocket ws;

  protected WsContext(Context ctx, WebSocket ws) {
    this.ctx = ctx;
    this.ws = ws;
  }

  /**
   * Serializes object to a JSON-string using the registered JsonMapper and sends it over the socket
   */
  public void send(Object message) {
    ws.send(ctx.jsonService().toJsonString(message));
  }

  /** Sends a String over the socket */
  public void send(String message) {
    ws.send(message);
  }

  /** Sends a byte[] over the socket */
  public void send(byte[] message) {
    ws.send(message);
  }

  /** Sends a ping over the socket */
  public void sendPing() {
    sendPing(null);
  }

  /** Sends a ping over the socket */
  public void sendPing(byte[] applicationData) {
    ws.ping(applicationData != null ? applicationData : new byte[0]);
  }

  /**
   * Return the request Context.
   *
   * @return the request
   */
  public Context ctx() {
    return ctx;
  }

  /**
   * Return the Websocket.
   *
   * @return the request
   */
  public WebSocket ws() {
    return ws;
  }

  /** Close the session */
  public void closeSession() {
    ws.close(CloseCode.NORMAL_CLOSURE, "cya", false);
  }

  /** Close the session with a CloseCode */
  public void closeSession(CloseCode code) {
    ws.close(code, "", false);
  }

  /** Close the session with a code and reason */
  public void closeSession(CloseCode code, String reason) {
    ws.close(code, reason, false);
  }

  public static final class WsOpen extends WsContext {
    WsOpen(Context ctx, WebSocket ws) {
      super(ctx, ws);
    }
  }

  public static final class WsPong extends WsMessageCtx {
    WsPong(Context ctx, WebSocket ws, WebSocketFrame wsFrame) {
      super(ctx, ws, wsFrame);
    }
  }

  public static final class WsError extends WsContext {
    private final Exception error;

    WsError(Context ctx, WebSocket ws, Exception error) {
      super(ctx, ws);
      this.error = error;
    }

    /** Get the Throwable error that occurred */
    public Exception error() {
      return error;
    }
  }

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

    /** The int status for why connection was closed */
    public CloseCode closeCode() {
      return closeCode;
    }

    /** The reason for the close */
    public String reason() {
      return reason;
    }

    /** True if the close was initiated by the remote endpoint */
    public boolean initiatedByRemote() {
      return initiatedByRemote;
    }
  }

  public abstract static sealed class WsMessageCtx extends WsContext {
    private final WebSocketFrame wsFrame;

    WsMessageCtx(Context ctx, WebSocket ws, WebSocketFrame wsFrame) {
      super(ctx, ws);
      this.wsFrame = wsFrame;
    }

    /** Get the underlying frame */
    public WebSocketFrame wsFrame() {
      return wsFrame;
    }

    /**
     * Indicates if this frame is the final fragment in a message.
     *
     * @return true if final fragment, false otherwise
     */
    public boolean isFin() {
      return wsFrame.isFin();
    }
  }

  public static final class WsBinaryMessage extends WsMessageCtx {
    private final byte[] data;

    WsBinaryMessage(Context ctx, WebSocket ws, WebSocketFrame wsFrame, byte[] data) {
      super(ctx, ws, wsFrame);
      this.data = data;
    }

    /** Get the binary data of the message */
    public byte[] data() {
      return data;
    }
  }

  public static final class WsMessage extends WsMessageCtx {
    private final String message;

    WsMessage(Context ctx, WebSocket ws, WebSocketFrame frame, String message) {
      super(ctx, ws, frame);
      this.message = message;
    }

    /** Receive a string message from the client */
    public String message() {
      return message;
    }

    /** Receive a message from the client as a class */
    public <T> T messageAsClass(Type type) {
      return ctx.jsonService().fromJson(type, message);
    }

    /** See Also: messageAsClass(Type) */
    public <T> T messageAsClass(Class<T> clazz) {
      return messageAsClass((Type) clazz);
    }
  }
}
