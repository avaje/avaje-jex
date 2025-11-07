package io.avaje.jex.http3.flupke.webtransport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import tech.kwik.flupke.webtransport.Session;
import tech.kwik.flupke.webtransport.WebTransportStream;

/**
 * 💡 The abstract sealed base class that provides the context for a specific {@link
 * WebTransportEvent}.
 *
 * <p>This class and its permitted subclasses represent the various lifecycle events that can occur
 * during a WebTransport session, such as opening, closing, and receiving new streams.
 */
public abstract sealed class WebTransportEvent {

  private final Session session;

  protected WebTransportEvent(Session session) {
    this.session = session;
  }

  /**
   * Returns the original path (including query parameters) of the URL that started this
   * WebTransport session.
   *
   * @return The URL path string.
   */
  public String path() {
    return session.getPath();
  }

  /**
   * Gets the unique ID associated with this WebTransport session.
   *
   * @return The session ID as a long.
   */
  public long sessionId() {
    return session.getSessionId();
  }

  /**
   * Creates a new unidirectional {@link OutputStream} from the server to the client within this
   * session.
   *
   * @return The newly created unidirectional wtStream.
   * @throws UncheckedIOException If an I/O error occurs while creating the wtStream.
   */
  public OutputStream createUnidirectionalStream() {
    try {
      return session.createUnidirectionalStream().getOutputStream();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Creates a new bidirectional {@link WebTransportStream} within this session, allowing data flow
   * in both directions.
   *
   * @return The newly created bidirectional wtStream.
   * @throws UncheckedIOException If an I/O error occurs while creating the wtStream.
   */
  public WebTransportStream createBiDirectionalStream() {
    try {
      return session.createBidirectionalStream();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Closes the WebTransport session gracefully with the default close code (0) and an empty reason.
   *
   * <p>This is equivalent to calling {@code closeSession(0, "")}.
   *
   * @throws UncheckedIOException If an I/O error occurs while closing the session.
   */
  public void closeSession() {
    closeSession(0, "");
  }

  /**
   * Closes the WebTransport session with a specified application-defined code and a reason string.
   *
   * @param code The application-specific close code.
   * @param message The human-readable reason for closing.
   * @throws UncheckedIOException If an I/O error occurs while closing the session.
   */
  public void closeSession(long code, String message) {

    try {
      session.close(code, message);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  // --- Nested Event Classes ---

  /**
   * Represents the context for an open event.
   *
   * <p>This event occurs when a new WebTransport connection is established and the handshake is
   * complete.
   */
  public static final class Open extends WebTransportEvent {
    Open(Session s) {
      super(s);
    }
  }

  /**
   * Represents the context for a close event.
   *
   * <p>This event is triggered when the WebTransport session is closed, either by the client, the
   * server (via {@link WebTransportEvent#closeSession(long, String)}), or due to a network error.
   */
  public static final class Close extends WebTransportEvent {

    final long code;
    final String message;

    Close(Session s, long code, String message) {
      super(s);
      this.code = code;
      this.message = message;
    }

    /**
     * Returns the application-defined close code sent by the party initiating the close.
     *
     * @return The close code.
     */
    public long code() {
      return code;
    }

    /**
     * Returns the reason message for the session close.
     *
     * @return The close reason message.
     */
    public String message() {
      return message;
    }
  }

  abstract static sealed class Stream extends WebTransportEvent implements AutoCloseable {
    final WebTransportStream wtStream;

    Stream(Session session, WebTransportStream stream) {
      super(session);
      this.wtStream = stream;
    }
  }

  /**
   * Represents the context for a new bidirectional wtStream event.
   *
   * <p>This occurs when the client initiates a new bidirectional wtStream to the server. The
   * returned wtStream can be used for both reading (input) and writing (output).
   */
  public static final class BiStream extends Stream {
    BiStream(Session session, WebTransportStream stream) {
      super(session, stream);
    }

    /**
     * Returns the bidirectional {@link WebTransportStream}.
     *
     * @return The wtStream for reading and writing data.
     */
    public InputStream requestStream() {
      return wtStream.getInputStream();
    }

    /**
     * Returns the wtStream for writing data to the client on this bidirectional wtStream.
     *
     * @return An {@link OutputStream} for writing to the client.
     */
    public OutputStream responseStream() {
      return wtStream.getOutputStream();
    }

    @Override
    public void close() throws IOException {
      try (var in = wtStream.getInputStream();
          var out = wtStream.getOutputStream()) {}
    }
  }

  /**
   * Represents the context for a new unidirectional wtStream event.
   *
   * <p>This occurs when the client initiates a new unidirectional wtStream to the server. The
   * returned wtStream is only for reading (input) data sent by the client.
   */
  public static final class UniStream extends Stream {

    UniStream(Session session, WebTransportStream stream) {
      super(session, stream);
    }

    /**
     * Returns the input wtStream for reading data sent by the client on this unidirectional
     * wtStream.
     *
     * @return An {@link InputStream} for reading the client's data.
     */
    public InputStream requestStream() {
      return wtStream.getInputStream();
    }

    @Override
    public void close() throws IOException {
      try (var in = wtStream.getInputStream()) {}
    }
  }
}
