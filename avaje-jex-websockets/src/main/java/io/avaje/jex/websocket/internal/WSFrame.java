package io.avaje.jex.websocket.internal;

/*
 * #%L
 * NanoHttpd-Websocket
 * %%
 * Copyright (C) 2012 - 2016 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import io.avaje.jex.websocket.WebSocketFrame;
import io.avaje.jex.websocket.exception.CloseCode;
import io.avaje.jex.websocket.exception.WebSocketException;

public sealed class WSFrame implements WebSocketFrame permits CloseFrame {

  static final Charset TEXT_CHARSET = StandardCharsets.UTF_8;

  static String binary2Text(byte[] payload) {
    return new String(payload, WSFrame.TEXT_CHARSET);
  }

  static String binary2Text(byte[] payload, int offset, int length) {
    return new String(payload, offset, length, WSFrame.TEXT_CHARSET);
  }

  private static int checkedRead(int read) throws IOException {
    if (read < 0) {
      throw new EOFException();
    }
    return read;
  }

  static WSFrame read(InputStream in) throws IOException {
    var head = (byte) checkedRead(in.read());
    var fin = (head & 0x80) != 0;
    var opCode = OpCode.find((byte) (head & 0x0F));
    if ((head & 0x70) != 0) {
      throw new WebSocketException(
          CloseCode.PROTOCOL_ERROR,
          "The reserved bits (" + Integer.toBinaryString(head & 0x70) + ") must be 0.");
    }
    if (opCode == null) {
      throw new WebSocketException(
          CloseCode.PROTOCOL_ERROR,
          "Received frame with reserved/unknown opcode " + (head & 0x0F) + ".");
    }
    if (opCode.isControlFrame() && !fin) {
      throw new WebSocketException(CloseCode.PROTOCOL_ERROR, "Fragmented control frame.");
    }

    var frame = new WSFrame(opCode, fin);
    frame.readPayloadInfo(in);
    frame.readPayload(in);
    if (frame.opCode() == OpCode.CLOSE) {
      return new CloseFrame(frame);
    }
    return frame;
  }

  static byte[] text2Binary(String payload) {
    return payload.getBytes(WSFrame.TEXT_CHARSET);
  }

  private OpCode opCode;

  private boolean fin;

  private byte[] maskingKey;

  private byte[] payload;

  // --------------------------------GETTERS---------------------------------

  private int payloadLength;

  private String payloadString;

  private WSFrame(OpCode opCode, boolean fin) {
    setOpCode(opCode);
    setFin(fin);
  }

  public WSFrame(OpCode opCode, boolean fin, byte[] payload) {
    this(opCode, fin, payload, null);
  }

  public WSFrame(OpCode opCode, boolean fin, byte[] payload, byte[] maskingKey) {
    this(opCode, fin);
    setMaskingKey(maskingKey);
    setBinaryPayload(payload);
  }

  public WSFrame(OpCode opCode, boolean fin, String payload) {
    this(opCode, fin, payload, null);
  }

  public WSFrame(OpCode opCode, boolean fin, String payload, byte[] maskingKey) {
    this(opCode, fin);
    setMaskingKey(maskingKey);
    setTextPayload(payload);
  }

  public WSFrame(OpCode opCode, List<WebSocketFrame> fragments) throws WebSocketException {
    setOpCode(opCode);
    setFin(true);

    var length = 0L;
    for (var inter : fragments) {
      length += inter.binaryPayload().length;
    }
    if (length < 0 || length > Integer.MAX_VALUE) {
      throw new WebSocketException(
          CloseCode.MESSAGE_TOO_BIG, "Max frame length has been exceeded.");
    }
    this.payloadLength = (int) length;
    var payload = new byte[this.payloadLength];
    var offset = 0;
    for (var inter : fragments) {
      System.arraycopy(inter.binaryPayload(), 0, payload, offset, inter.binaryPayload().length);
      offset += inter.binaryPayload().length;
    }
    setBinaryPayload(payload);
  }

  public WSFrame(WSFrame clone) {
    setOpCode(clone.opCode());
    setFin(clone.isFin());
    setBinaryPayload(clone.binaryPayload());
    setMaskingKey(clone.maskingKey());
  }

  @Override
  public byte[] binaryPayload() {
    return this.payload;
  }

  @Override
  public byte[] maskingKey() {
    return this.maskingKey;
  }

  @Override
  public OpCode opCode() {
    return this.opCode;
  }

  // --------------------------------SERIALIZATION---------------------------

  @Override
  public String textPayload() {
    if (this.payloadString == null) {
      this.payloadString = binary2Text(binaryPayload());
    }
    return this.payloadString;
  }

  @Override
  public boolean isFin() {
    return this.fin;
  }

  @Override
  public boolean isMasked() {
    return this.maskingKey != null && this.maskingKey.length == 4;
  }

  private String payloadToString() {
    if (this.payload == null) {
      return "null";
    }
    final var sb = new StringBuilder();
    sb.append('[').append(this.payload.length).append("b] ");
    if (opCode() == OpCode.TEXT) {
      var text = textPayload();
      if (text.length() > 100) {
        sb.append(text.substring(0, 100)).append("...");
      } else {
        sb.append(text);
      }
    } else {
      sb.append("0x");
      for (var i = 0; i < Math.min(this.payload.length, 50); ++i) {
        sb.append(Integer.toHexString(this.payload[i] & 0xFF));
      }
      if (this.payload.length > 50) {
        sb.append("...");
      }
    }
    return sb.toString();
  }

  private void readPayload(InputStream in) throws IOException {
    this.payload = new byte[this.payloadLength];
    var read = 0;
    while (read < this.payloadLength) {
      read += checkedRead(in.read(this.payload, read, this.payloadLength - read));
    }

    if (isMasked()) {
      for (var i = 0; i < this.payload.length; i++) {
        this.payload[i] ^= this.maskingKey[i % 4];
      }
    }

    // Test for Unicode errors
    if (opCode() == OpCode.TEXT) {
      this.payloadString = binary2Text(binaryPayload());
    }
  }

  // --------------------------------ENCODING--------------------------------

  private void readPayloadInfo(InputStream in) throws IOException {
    var b = (byte) checkedRead(in.read());
    var masked = (b & 0x80) != 0;

    this.payloadLength = (byte) (0x7F & b);
    if (this.payloadLength == 126) {
      // checkedRead must return int for this to work
      this.payloadLength = (checkedRead(in.read()) << 8 | checkedRead(in.read())) & 0xFFFF;
      if (this.payloadLength < 126) {
        throw new WebSocketException(
            CloseCode.PROTOCOL_ERROR,
            "Invalid data frame 2byte length. (not using minimal length encoding)");
      }
    } else if (this.payloadLength == 127) {
      var length =
          (long) checkedRead(in.read()) << 56
              | (long) checkedRead(in.read()) << 48
              | (long) checkedRead(in.read()) << 40
              | (long) checkedRead(in.read()) << 32
              | checkedRead(in.read()) << 24
              | checkedRead(in.read()) << 16
              | checkedRead(in.read()) << 8
              | checkedRead(in.read());
      if (length < 65536) {
        throw new WebSocketException(
            CloseCode.PROTOCOL_ERROR,
            "Invalid data frame 4byte length. (not using minimal length encoding)");
      }
      if (length < 0 || length > Integer.MAX_VALUE) {
        throw new WebSocketException(
            CloseCode.MESSAGE_TOO_BIG, "Max frame length has been exceeded.");
      }
      this.payloadLength = (int) length;
    }

    if (this.opCode.isControlFrame()) {
      if (this.payloadLength > 125) {
        throw new WebSocketException(
            CloseCode.PROTOCOL_ERROR, "Control frame with payload length > 125 bytes.");
      }
      if (this.opCode == OpCode.CLOSE && this.payloadLength == 1) {
        throw new WebSocketException(
            CloseCode.PROTOCOL_ERROR, "Received close frame with payload len 1.");
      }
    }

    if (masked) {
      this.maskingKey = new byte[4];
      var read = 0;
      while (read < this.maskingKey.length) {
        read += checkedRead(in.read(this.maskingKey, read, this.maskingKey.length - read));
      }
    }
  }

  void setBinaryPayload(byte[] payload) {
    this.payload = payload;
    this.payloadLength = payload.length;
    this.payloadString = null;
  }

  void setFin(boolean fin) {
    this.fin = fin;
  }

  void setMaskingKey(byte[] maskingKey) {
    if (maskingKey != null && maskingKey.length != 4) {
      throw new IllegalArgumentException(
          "MaskingKey " + Arrays.toString(maskingKey) + " hasn't length 4");
    }
    this.maskingKey = maskingKey;
  }

  void setOpCode(OpCode opcode) {
    this.opCode = opcode;
  }

  void setTextPayload(String payload) {
    this.payload = text2Binary(payload);
    this.payloadLength = payload.length();
    this.payloadString = payload;
  }

  // --------------------------------CONSTANTS-------------------------------

  void setUnmasked() {
    setMaskingKey(null);
  }

  @Override
  public String toString() {
    final var sb = new StringBuilder("WS[");
    sb.append(opCode());
    sb.append(", ").append(isFin() ? "fin" : "inter");
    sb.append(", ").append(isMasked() ? "masked" : "unmasked");
    sb.append(", ").append(payloadToString());
    sb.append(']');
    return sb.toString();
  }

  // ------------------------------------------------------------------------
  void write(OutputStream out) throws IOException {
    byte header = 0;
    if (this.fin) {
      header |= 0x80;
    }
    header |= this.opCode.value() & 0x0F;
    out.write(header);

    this.payloadLength = binaryPayload().length;
    if (this.payloadLength <= 125) {
      out.write(isMasked() ? 0x80 | (byte) this.payloadLength : (byte) this.payloadLength);
    } else {
      if (this.payloadLength <= 0xFFFF) {
        out.write(isMasked() ? 0xFE : 126);
      } else {
        out.write(isMasked() ? 0xFF : 127);
        out.write(this.payloadLength >>> 56 & 0); // integer only
        // contains
        // 31 bit
        out.write(this.payloadLength >>> 48 & 0);
        out.write(this.payloadLength >>> 40 & 0);
        out.write(this.payloadLength >>> 32 & 0);
        out.write(this.payloadLength >>> 24);
        out.write(this.payloadLength >>> 16);
      }
      out.write(this.payloadLength >>> 8);
      out.write(this.payloadLength);
    }

    if (isMasked()) {
      out.write(this.maskingKey);
      for (var i = 0; i < this.payloadLength; i++) {
        out.write(binaryPayload()[i] ^ this.maskingKey[i % 4]);
      }
    } else {
      out.write(binaryPayload());
    }
    out.flush();
  }
}
