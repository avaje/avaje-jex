package io.avaje.jex.websocket.exception;

public class WebSocketException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final CloseCode code;

  private final String reason;

  public WebSocketException(CloseCode code, String reason) {
    this(code, reason, null);
  }

  public WebSocketException(CloseCode code, String reason, Exception cause) {
    super(code + ": " + reason, cause);
    this.code = code;
    this.reason = reason;
  }

  public WebSocketException(Exception cause) {
    this(CloseCode.INTERNAL_SERVER_ERROR, cause.toString(), cause);
  }

  public CloseCode getCode() {
    return this.code;
  }

  public String getReason() {
    return this.reason;
  }
}
