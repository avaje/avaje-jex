package io.avaje.jex.http.sse;

import java.io.Closeable;
import java.util.function.Consumer;

import io.avaje.jex.http.Context;
import io.avaje.jex.http.ExchangeHandler;
import io.avaje.jex.spi.JsonService;

/**
 * A client for Server-Sent Events (SSE). This class handles the setup of the SSE connection,
 * sending events and comments to the client, and managing the lifecycle of the connection. It
 * ensures proper headers are set and provides methods for sending various types of data.
 *
 * <p>This class implements {@link Closeable} to allow for proper resource management. The
 * connection is automatically closed if the client disconnects or if an error occurs during event
 * emission.
 */
public interface SseClient extends Closeable {

  /** Return an SseClient handler. */
  static ExchangeHandler handler(Consumer<SseClient> consumer) {
    return new SseHandler(consumer);
  }

  /** Close the SseClient and release keepAlive block if any */
  @Override
  void close();

  /**
   * Return the request Context.
   *
   * @return the request
   */
  Context ctx();

  /**
   * By blocking the SSE connection, you can share this client outside the handler to notify it from
   * other sources. Keep in mind that this function will block the handler until the SSE client is
   * released by another thread.
   */
  void keepAlive();

  /**
   * Attempt to send a comment. If the {@link Emitter} fails to emit (remote client has
   * disconnected), the {@link #close()} function will be called instead.
   */
  void sendComment(String comment);

  /** Calls {@link #sendEvent(String, Object, String)} with event set to "message" */
  void sendEvent(Object data);

  /** Calls {@link #sendEvent(String, Object, String)} with id set to null */
  void sendEvent(String event, Object data);

  /**
   * Attempt to send an event. If the {@link Emitter} fails to emit (remote client has
   * disconnected), the {@link #close()} function will be called instead.
   *
   * @param event The name of the event.
   * @param data The data to send in the event. This can be a String, an InputStream, or any object
   *     that can be serialized to JSON using the configured {@link JsonService}.
   * @param id The ID of the event.
   */
  void sendEvent(String event, Object data, String id);

  /**
   * Returns true if {@link #close()} has been called. This can either be by the user, or by Jex
   * upon detecting that the {@link Emitter} is closed.
   */
  boolean terminated();
}
