package io.avaje.jex.http;

import io.avaje.jex.Routing;

/**
 * The routing error handler. Can be mapped to the error cause in {@link Routing}.
 *
 * @param <T> type of throwable handled by this handler
 */
@FunctionalInterface
public interface ExceptionHandler<T extends Exception> {

  /**
   * Error handling consumer. Do not throw an exception from an error handler, it would make this
   * error handler invalid and the exception would be ignored.
   *
   * @param ctx the server context
   * @param exception the cause of the error
   */
  void handle(Context ctx, T exception);
}
