package io.avaje.jex.websocket.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Websocket Close Codes. These codes are used to indicate the reason why a WebSocket connection has
 * been closed.
 */
public enum CloseCode {
  NORMAL_CLOSURE(1000),
  GOING_AWAY(1001),
  PROTOCOL_ERROR(1002),
  UNSUPPORTED_DATA(1003),
  NO_STATUS_RCVD(1005),
  ABNORMAL_CLOSURE(1006),
  INVALID_FRAME_PAYLOAD_DATA(1007),
  POLICY_VIOLATION(1008),
  MESSAGE_TOO_BIG(1009),
  MANDATORY_EXT(1010),
  INTERNAL_SERVER_ERROR(1011),
  TLS_HANDSHAKE(1015);

  private final int code;

  private static final Map<Integer, CloseCode> CODES_MAP = HashMap.newHashMap(values().length);

  static {
    for (CloseCode code : values()) {
      CODES_MAP.put(code.code(), code);
    }
  }

  CloseCode(int code) {
    this.code = code;
  }

  /**
   * Returns the integer value of this close code.
   *
   * @return The integer close code.
   */
  public int code() {
    return this.code;
  }

  /**
   * Finds the {@code CloseCode} enum constant corresponding to the given integer value.
   *
   * @param value The integer value of the close code to find.
   * @return The corresponding {@code CloseCode} enum constant, or {@code null} if no match is
   *     found.
   */
  public static CloseCode find(int value) {
    return CODES_MAP.get(value);
  }
}
