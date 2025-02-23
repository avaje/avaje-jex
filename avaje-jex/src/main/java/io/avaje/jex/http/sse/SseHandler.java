package io.avaje.jex.http.sse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

import io.avaje.jex.core.Constants;
import io.avaje.jex.http.BadRequestException;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.ExchangeHandler;

/** Handler that configures a request for Server Sent Events */
public class SseHandler implements ExchangeHandler {

  private static final String TEXT_EVENT_STREAM = "text/event-stream";
  private final Consumer<SseClient> consumer;

  public SseHandler(Consumer<SseClient> consumer) {
    this.consumer = consumer;
  }

  @Override
  public void handle(Context ctx) throws Exception {

    if (ctx.header(Constants.ACCEPT) == null
        && !TEXT_EVENT_STREAM.equals(ctx.header(Constants.ACCEPT)))
      throw new BadRequestException("SSE Requests must have an 'Accept: text/event-stream' header");

    final var exchange = ctx.exchange();
    final var headers = exchange.getResponseHeaders();
    headers.add(Constants.CONTENT_TYPE, TEXT_EVENT_STREAM);
    headers.add(Constants.CONTENT_ENCODING, "UTF-8");
    headers.add("Connection", "close");
    headers.add("Cache-Control", "no-cache");
    headers.add("X-Accel-Buffering", "no"); // See https://serverfault.com/a/801629

    try (var sse = new SseClient(ctx)) {
      exchange.sendResponseHeaders(200, 0);
      consumer.accept(sse);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
