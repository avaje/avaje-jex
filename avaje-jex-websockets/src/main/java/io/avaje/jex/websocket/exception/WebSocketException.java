package io.avaje.jex.websocket.exception;

/**
 * An unchecked exception specifically for signaling errors that occur during WebSocket
 * communication. This exception wraps a standard {@link CloseCode} and a descriptive reason, making
 * it easy to communicate the cause of a connection failure or protocol violation in a way that
 * aligns with the WebSocket protocol.
 */
public class WebSocketException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final CloseCode code;

  private final String reason;

  /**
   * Constructs a new {@code WebSocketException} with a specific close code and reason. The
   * exception will have no cause.
   *
   * @param code The {@link CloseCode} that indicates the nature of the error.
   * @param reason A descriptive message explaining the error.
   */
  public WebSocketException(CloseCode code, String reason) {
    this(code, reason, null);
  }

  /**
   * Constructs a new {@code WebSocketException} with a specific close code, reason, and a cause.
   *
   * @param code The {@link CloseCode} that indicates the nature of the error.
   * @param reason A descriptive message explaining the error.
   * @param cause The underlying exception that caused this {@code WebSocketException}.
   */
  public WebSocketException(CloseCode code, String reason, Exception cause) {
    super(code + ": " + reason, cause);
    this.code = code;
    this.reason = reason;
  }

  /**
   * Constructs a new {@code WebSocketException} from an existing exception. It defaults to {@link
   * CloseCode#INTERNAL_SERVER_ERROR} for the close code, using the cause's {@code toString()}
   * method for the reason.
   *
   * @param cause The underlying exception that caused this {@code WebSocketException}.
   */
  public WebSocketException(Exception cause) {
    this(CloseCode.INTERNAL_SERVER_ERROR, cause.toString(), cause);
  }

  /**
   * Returns the WebSocket close code associated with this exception.
   *
   * @return The {@link CloseCode} enum value.
   */
  public CloseCode code() {
    return this.code;
  }

  /**
   * Returns the descriptive reason associated with this exception.
   *
   * @return The descriptive reason string.
   */
  public String reason() {
    return this.reason;
  }
}
