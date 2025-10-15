package io.avaje.jex.websocket.internal;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;

import java.io.EOFException;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.net.httpserver.HttpExchange;

import io.avaje.jex.websocket.WebSocket;
import io.avaje.jex.websocket.WebSocketFrame;
import io.avaje.jex.websocket.WebSocketFrame.OpCode;
import io.avaje.jex.websocket.exception.CloseCode;
import io.avaje.jex.websocket.exception.WebSocketException;

public abstract class AbstractWebSocket implements WebSocket {

  private final List<WebSocketFrame> continuousFrames = new LinkedList<>();

  private OpCode continuousOpCode = null;
  private final InputStream in;
  private Lock lock = new ReentrantLock();
  protected final System.Logger log = System.getLogger("io.avaje.jex.websocket");
  private final OutputStream out;
  private volatile State state = State.UNCONNECTED;
  private final URI uri;

  protected AbstractWebSocket(HttpExchange exchange) {
    this.uri = exchange.getRequestURI();
    log.log(INFO, "connecting websocket {0}", uri);
    this.state = State.CONNECTING;
    this.in = exchange.getRequestBody();
    this.out = exchange.getResponseBody();
  }

  @Override
  public void close(CloseCode code, String reason, boolean initiatedByRemote) {
    log.log(INFO, "closing websocket {0}", uri);

    var oldState = this.state;
    this.state = State.CLOSING;
    if (oldState == State.OPEN) {
      sendFrame(new CloseFrame(code, reason));
    } else {
      doClose(code, reason, initiatedByRemote);
    }
  }

  void doClose(CloseCode code, String reason, boolean initiatedByRemote) {
    if (this.state == State.CLOSED) {
      return;
    }
    try (in; out) {
      // just close the streams
    } catch (IOException expected) {
      // Expected
    }
    this.state = State.CLOSED;
    onClose(code, reason, initiatedByRemote);
  }

  private void handleCloseFrame(WebSocketFrame frame) {
    var code = CloseCode.NORMAL_CLOSURE;
    var reason = "";
    if (frame instanceof CloseFrame cf) {
      code = cf.getCloseCode();
      reason = cf.getCloseReason();
    }
    log.log(
        TRACE,
        "handleCloseFrame: {0}, code={1}, reason={2}, state {3}",
        uri,
        code,
        reason,
        this.state);
    if (this.state == State.CLOSING) {
      // Answer for my requested close
      doClose(code, reason, false);
    } else {
      close(code, reason, true);
    }
  }

  private void handleFrameFragment(WebSocketFrame frame) {
    if (frame.opCode() != OpCode.CONTINUATION) {
      // First
      if (this.continuousOpCode != null) {
        throw new WebSocketException(
            CloseCode.PROTOCOL_ERROR, "Previous continuous frame sequence not completed.");
      }
      this.continuousOpCode = frame.opCode();
      this.continuousFrames.clear();
      this.continuousFrames.add(frame);
    } else if (frame.isFin()) {
      // Last
      if (this.continuousOpCode == null) {
        throw new WebSocketException(
            CloseCode.PROTOCOL_ERROR, "Continuous frame sequence was not started.");
      }
      this.continuousFrames.add(frame);
      onMessage(new WSFrame(this.continuousOpCode, this.continuousFrames));
      this.continuousOpCode = null;
      this.continuousFrames.clear();
    } else if (this.continuousOpCode == null) {
      // Unexpected
      throw new WebSocketException(
          CloseCode.PROTOCOL_ERROR, "Continuous frame sequence was not started.");
    } else {
      // Intermediate
      this.continuousFrames.add(frame);
    }
  }

  private void handleWebsocketFrame(WebSocketFrame frame) {
    onFrameReceived(frame);
    if (frame.opCode() == OpCode.CLOSE) {
      handleCloseFrame(frame);
    } else if (frame.opCode() == OpCode.PING) {
      sendFrame(new WSFrame(OpCode.PONG, true, frame.binaryPayload()));
    } else if (frame.opCode() == OpCode.PONG) {
      onPong(frame);
    } else if (!frame.isFin() || frame.opCode() == OpCode.CONTINUATION) {
      handleFrameFragment(frame);
    } else if (this.continuousOpCode != null) {
      throw new WebSocketException(
          CloseCode.PROTOCOL_ERROR, "Continuous frame sequence not completed.");
    } else if (frame.opCode() == OpCode.TEXT || frame.opCode() == OpCode.BINARY) {
      onMessage(frame);
    } else {
      throw new WebSocketException(
          CloseCode.PROTOCOL_ERROR, "Non control or continuous frame expected.");
    }
  }

  @Override
  public boolean isOpen() {
    return state == State.OPEN;
  }

  protected void onFrameReceived(WebSocketFrame frame) {
    log.log(TRACE, "frame received: {0}", frame);
  }

  /**
   * Debug method. <b>Do not Override unless for debug purposes!</b><br>
   * This method is called before actually sending the frame.
   *
   * @param frame The sent WebSocket Frame.
   */
  protected void onFrameSent(WebSocketFrame frame) {
    log.log(TRACE, "frame sent: {0}", frame);
  }

  protected abstract void onClose(CloseCode code, String reason, boolean initiatedByRemote);

  protected abstract void onError(Exception exception);

  protected abstract void onMessage(WebSocketFrame message) throws WebSocketException;

  protected abstract void onOpen() throws WebSocketException;

  protected abstract void onPong(WebSocketFrame pong) throws WebSocketException;

  @Override
  public void ping(byte[] payload) {
    sendFrame(new WSFrame(OpCode.PING, true, payload));
  }

  void readWebsocket() {
    try {
      state = State.OPEN;
      log.log(DEBUG, "websocket open {0}", uri);
      onOpen();
      while (this.state == State.OPEN) {
        handleWebsocketFrame(WSFrame.read(in));
      }
    } catch (EOFException e) {
      log.log(TRACE, "exception on websocket", e);
      onError(e);
      doClose(CloseCode.ABNORMAL_CLOSURE, e.toString(), false);
    } catch (Exception e) {
      onError(e);
      if (e instanceof WebSocketException wse) {
        doClose(wse.getCode(), wse.getReason(), false);
      } else {
        doClose(CloseCode.ABNORMAL_CLOSURE, e.toString(), false);
      }
    } finally {
      doClose(
          CloseCode.INTERNAL_SERVER_ERROR,
          "Handler terminated without closing the connection.",
          false);
      log.log(TRACE, "readWebsocket() exiting {0}", uri);
    }
  }

  @Override
  public void send(byte[] payload) {
    sendFrame(new WSFrame(OpCode.BINARY, true, payload));
  }

  @Override
  public void send(String payload) {
    sendFrame(new WSFrame(OpCode.TEXT, true, payload));
  }

  public void sendFrame(WSFrame frame) {
    lock.lock();
    try {
      onFrameSent(frame);
      frame.write(this.out);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } finally {
      lock.unlock();
    }
  }
}
