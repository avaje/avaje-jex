package io.avaje.jex.http.sse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.function.Consumer;

import io.avaje.jex.core.Constants;
import io.avaje.jex.http.BadRequestException;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.ExchangeHandler;

/** Handler that configures a request for Server Sent Events */
final class SseHandler implements ExchangeHandler {

  private static final String TEXT_EVENT_STREAM = "text/event-stream";
  private final Consumer<SseClient> consumer;

  SseHandler(Consumer<SseClient> consumer) {
    this.consumer = consumer;
  }

  @Override
  public void handle(Context ctx) throws Exception {

    if (!Optional.ofNullable(ctx.header(Constants.ACCEPT))
        .filter(s -> s.contains(TEXT_EVENT_STREAM))
        .isPresent()) {
      throw new BadRequestException("SSE Requests must have an 'Accept: text/event-stream' header");
    }
    final var exchange = ctx.exchange();
    final var headers = exchange.getResponseHeaders();
    headers.add(Constants.CONTENT_TYPE, TEXT_EVENT_STREAM);
    headers.add(Constants.CONTENT_ENCODING, "UTF-8");
    headers.add("Connection", "close");
    headers.add("Cache-Control", "no-cache");
    headers.add("X-Accel-Buffering", "no"); // See https://serverfault.com/a/801629

    try (var sse = new SseClientImpl(ctx)) {
      exchange.sendResponseHeaders(200, 0);
      consumer.accept(sse);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
