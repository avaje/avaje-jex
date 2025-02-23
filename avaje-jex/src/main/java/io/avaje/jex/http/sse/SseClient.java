package io.avaje.jex.http.sse;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.lang.System.Logger.Level;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import io.avaje.jex.http.Context;
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
public class SseClient implements Closeable {

  private static final System.Logger log = System.getLogger(SseClient.class.getCanonicalName());

  private final AtomicBoolean terminated = new AtomicBoolean(false);
  private final Emitter emitter;
  private final JsonService jsonService;
  private final Context ctx;
  private CompletableFuture<?> blockingFuture;

  SseClient(Context ctx) {
    this.emitter = new Emitter(ctx.exchange().getResponseBody());
    jsonService = ctx.jsonService();
    this.ctx = ctx;
  }

  /** Close the SseClient */
  @Override
  public void close() {
    if (terminated.getAndSet(true) && blockingFuture != null) {
      blockingFuture.complete(null);
    }
  }

  /**
   * Return the request Context.
   *
   * @return the request
   */
  public Context ctx() {
    return ctx;
  }

  /**
   * By blocking the SSE connection, you can share this client outside the handler to notify it from
   * other sources. Keep in mind that this function will block the handler until the SSE client is
   * released by another thread.
   */
  public void keepAlive() {
    this.blockingFuture = new CompletableFuture<>();
    blockingFuture.join();
  }

  /**
   * Attempt to send a comment. If the {@link Emitter} fails to emit (remote client has
   * disconnected), the {@link #close()} function will be called instead.
   */
  public void sendComment(String comment) {
    if (terminated.get()) {
      logTerminated();
      return;
    }
    emitter.emit(comment);
    if (emitter.isClosed()) { // can't detect if closed before we try emitting
      close();
    }
  }

  /** Calls {@link #sendEvent(String, Object, String)} with event set to "message" */
  public void sendEvent(Object data) {
    sendEvent("message", data);
  }

  /** Calls {@link #sendEvent(String, Object, String)} with id set to null */
  public void sendEvent(String event, Object data) {
    sendEvent(event, data, null);
  }

  /**
   * Attempt to send an event. If the {@link Emitter} fails to emit (remote client has
   * disconnected), the {@link #close()} function will be called instead.
   *
   * @param event The name of the event.
   * @param data The data to send in the event. This can be a String, an InputStream, or any object
   *     that can be serialized to JSON using the configured {@link JsonService}.
   * @param id The ID of the event.
   */
  public void sendEvent(String event, Object data, String id) {
    if (terminated.get()) {
      logTerminated();
      return;
    }

    final var inputStream =
        switch (data) {
          case final InputStream is -> is;
          case final String s -> new ByteArrayInputStream(s.getBytes(UTF_8));
          default -> new ByteArrayInputStream(jsonService.toJsonString(data).getBytes(UTF_8));
        };

    emitter.emit(event, inputStream, id);

    if (emitter.isClosed()) { // can't detect if closed before we try emitting
      close();
    }
  }

  /**
   * Returns true if {@link #close()} has been called. This can either be by the user, or by Jex
   * upon detecting that the {@link Emitter} is closed.
   */
  public boolean terminated() {
    return terminated.get();
  }

  private void logTerminated() {
    log.log(Level.WARNING, "Cannot send data, SseClient has been terminated.");
  }
}
