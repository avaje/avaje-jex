package io.avaje.jex.http3.flupke.webtransport;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import io.avaje.jex.http.Context;
import tech.kwik.flupke.webtransport.Session;
import tech.kwik.flupke.webtransport.WebTransportStream;

/**
 * The abstract sealed base class that provides the context for a specific WebTransport event.
 *
 * <p>This class encapsulates the underlying {@link Context} (request context) and the {@link
 * WebTransport} connection, offering methods for sending messages and controlling the session.
 * Subclasses represent specific WebTransport events (e.g., Open, Message, Close, Error).
 */
public abstract sealed class WtContext {

  private final Session session;

  protected WtContext(Session session) {
    this.session = session;
  }

  /** The path (including query parameters) of the URL that started this session. */
  public String path() {
    return session.getPath();
  }

  /** Gets the session id. */
  public long sessionId() {
    return session.getSessionId();
  }

  /**
   * Sends a {@code byte[]} message (Binary Frame) over the socket.
   *
   * @param message The binary data to send.
   * @return
   */
  public WebTransportStream createUnidirectionalStream() {
    try {
      return session.createUnidirectionalStream();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public WebTransportStream createBiDirectionalStream() {
    try {
      return session.createBidirectionalStream();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** Closes the WebTransport session gracefully with a default reason. */
  public void closeSession() {
    closeSession(0, "");
  }

  /**
   * Closes the WebTransport session with a specified {@link CloseCode} and an empty reason string.
   *
   * @param code The {@link CloseCode} to send.
   */
  public void closeSession(long code, String message) {

    try {
      session.close(code, message);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Represents the context for an open event. This event occurs when a new connection is
   * established and the handshake is complete.
   */
  public static final class WtOpen extends WtContext {
    WtOpen(Session s) {
      super(s);
    }
  }

  /**
   * Represents the context for an open event. This event occurs when a new connection is
   * established and the handshake is complete.
   */
  public static final class WtClose extends WtContext {

    final long code;
    final String message;

    WtClose(Session s, long code, String message) {
      super(s);
      this.code = code;
      this.message = message;
    }

    public long code() {
      return code;
    }

    public String message() {
      return message;
    }
  }

  /**
   * Represents the context for an error event. This is triggered when an unhandled exception occurs
   * during the lifecycle of the connection.
   */
  public static final class WtError extends WtContext {
    private final Exception error;

    WtError(Session session, Exception error) {
      super(session);
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
   * The abstract sealed base class for WebTransport contexts that involve receiving a data frame
   * (e.g., Text, Binary, Pong).
   */
  static sealed class WtStream extends WtContext {
    private final WebTransportStream stream;

    WtStream(Session session, WebTransportStream stream) {
      super(session);
      this.stream = stream;
    }

    WebTransportStream stream() {
      return stream;
    }
  }

  /**
   * The abstract sealed base class for WebTransport contexts that involve receiving a data frame
   * (e.g., Text, Binary, Pong).
   */
  public static final class BiStream extends WtStream {
    BiStream(Session session, WebTransportStream stream) {
      super(session, stream);
    }

    public WebTransportStream requestStream() {
      return stream();
    }
  }

  /**
   * The abstract sealed base class for WebTransport contexts that involve receiving a data frame
   * (e.g., Text, Binary, Pong).
   */
  public static final class UniStream extends WtStream  {
    private final InputStream stream;

    UniStream(Session session, WebTransportStream stream) {
      super(session, stream);
      this.stream = stream.getInputStream();
    }

    public InputStream requestStream() {
      return stream;
    }
  }
}
