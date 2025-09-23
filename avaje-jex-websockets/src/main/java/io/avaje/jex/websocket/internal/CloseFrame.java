package io.avaje.jex.websocket.internal;

import io.avaje.jex.websocket.exception.CloseCode;

public final class CloseFrame extends WSFrame {

  private static byte[] generatePayload(CloseCode code, String closeReason) {
    if (code != null) {
      var reasonBytes = text2Binary(closeReason);
      var payload = new byte[reasonBytes.length + 2];
      payload[0] = (byte) (code.getValue() >> 8 & 0xFF);
      payload[1] = (byte) (code.getValue() & 0xFF);
      System.arraycopy(reasonBytes, 0, payload, 2, reasonBytes.length);
      return payload;
    }
    return new byte[0];
  }

  private CloseCode closeCode;

  private String closeReason;

  public CloseFrame(CloseCode code, String closeReason) {
    super(OpCode.CLOSE, true, generatePayload(code, closeReason));
  }

  CloseFrame(WSFrame wrap) {
    super(wrap);
    assert wrap.opCode() == OpCode.CLOSE;
    if (wrap.binaryPayload().length >= 2) {
      this.closeCode =
          CloseCode.find((wrap.binaryPayload()[0] & 0xFF) << 8 | wrap.binaryPayload()[1] & 0xFF);
      this.closeReason = binary2Text(binaryPayload(), 2, binaryPayload().length - 2);
    }
  }

  public CloseCode getCloseCode() {
    return this.closeCode;
  }

  public String getCloseReason() {
    return this.closeReason;
  }
}
