package io.avaje.jex.websocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a WebSocket frame as defined by RFC 6455. Provides access to frame payload, masking,
 * opcode, and frame control information.
 */
public interface WebSocketFrame {

  /**
   * Returns the binary payload of the frame.
   *
   * @return the binary payload as a byte array
   */
  byte[] binaryPayload();

  /**
   * Returns the masking key used for the frame, if present.
   *
   * @return the masking key as a byte array, or null if not masked
   */
  byte[] maskingKey();

  /**
   * Returns the opcode of the frame.
   *
   * @return the opcode
   */
  OpCode opCode();

  /**
   * Returns the text payload of the frame, if applicable.
   *
   * @return the text payload, or null if not a text frame
   */
  String textPayload();

  /**
   * Indicates if this frame is the final fragment in a message.
   *
   * @return true if final fragment, false otherwise
   */
  boolean isFin();

  /**
   * Indicates if the frame is masked.
   *
   * @return true if masked, false otherwise
   */
  boolean isMasked();

  /**
   * Writes the frame to the given output stream in WebSocket frame format.
   *
   * @param out the output stream to write to
   * @throws IOException if an I/O error occurs
   */
  void write(OutputStream out) throws IOException;

  /** WebSocket opcodes */
  public enum OpCode {
    CONTINUATION(0),
    TEXT(1),
    BINARY(2),
    CLOSE(8),
    PING(9),
    PONG(10);

    private final byte code;
    private static final Map<Byte, OpCode> VALUES =
        Arrays.stream(values()).collect(Collectors.toMap(OpCode::value, e -> e));

    OpCode(int code) {
      this.code = (byte) code;
    }

    /**
     * Finds the OpCode corresponding to the given byte value.
     *
     * @param value the opcode value
     * @return the matching OpCode, or null if not found
     */
    public static OpCode find(byte value) {
      return VALUES.get(value);
    }

    /**
     * Returns the byte value of this opcode.
     *
     * @return the opcode value
     */
    public byte value() {
      return this.code;
    }

    /**
     * Indicates if this opcode is a control frame (close, ping, pong).
     *
     * @return true if control frame, false otherwise
     */
    public boolean isControlFrame() {
      return this == CLOSE || this == PING || this == PONG;
    }
  }
}
