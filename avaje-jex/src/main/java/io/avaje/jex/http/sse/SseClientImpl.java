package io.avaje.jex.http.sse;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.System.Logger.Level;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import io.avaje.jex.http.Context;
import io.avaje.jex.spi.JsonService;

final class SseClientImpl implements SseClient {

  private static final System.Logger log = System.getLogger(SseClient.class.getCanonicalName());

  private final AtomicBoolean terminated = new AtomicBoolean(false);
  private final Emitter emitter;
  private final JsonService jsonService;
  private final Context ctx;
  private CompletableFuture<?> blockingFuture;

  SseClientImpl(Context ctx) {
    this.emitter = new Emitter(ctx.exchange().getResponseBody());
    jsonService = ctx.jsonService();
    this.ctx = ctx;
  }

  @Override
  public void close() {
    if (terminated.getAndSet(true) && blockingFuture != null) {
      blockingFuture.complete(null);
    }
  }

  @Override
  public Context ctx() {
    return ctx;
  }

  @Override
  public void keepAlive() {

    if (terminated.get()) return;

    this.blockingFuture = new CompletableFuture<>();
    blockingFuture.join();
  }

  private void logTerminated() {
    log.log(Level.WARNING, "Cannot send data, SseClient has been terminated.");
  }

  @Override
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

  @Override
  public void sendEvent(Object data) {
    sendEvent("message", data);
  }

  @Override
  public void sendEvent(String event, Object data) {
    sendEvent(event, data, null);
  }

  @Override
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

  @Override
  public boolean terminated() {
    return terminated.get();
  }
}
