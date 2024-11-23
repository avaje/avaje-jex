package io.avaje.jex.core;

import static java.lang.System.Logger.Level.WARNING;

import java.util.Map;

import io.avaje.applog.AppLog;
import io.avaje.jex.ExceptionHandler;
import io.avaje.jex.http.ErrorCode;
import io.avaje.jex.http.HttpResponseException;
import io.avaje.jex.spi.HeaderKeys;
import io.avaje.jex.spi.SpiContext;

public final class ExceptionManager {

  private static final String APPLICATION_JSON = "application/json";

  private static final System.Logger log = AppLog.getLogger("io.avaje.jex");

  private final Map<Class<?>, ExceptionHandler<?>> handlers;

  public ExceptionManager(Map<Class<?>, ExceptionHandler<?>> handlers) {
    this.handlers = handlers;
  }

  @SuppressWarnings("unchecked")
  public <T extends Exception> ExceptionHandler<Exception> find(Class<T> exceptionType) {
    Class<?> type = exceptionType;
    do {
      final ExceptionHandler<?> handler = handlers.get(type);
      if (handler != null) {
        return (ExceptionHandler<Exception>) handler;
      }
      type = type.getSuperclass();
    } while (type != null);
    return null;
  }

  void handle(SpiContext ctx, Exception e) {
    final ExceptionHandler<Exception> handler = find(e.getClass());
    if (handler != null) {
      handler.handle(e, ctx);
    } else if (e instanceof HttpResponseException ex) {
      defaultHandling(ctx, ex);
    } else {
      unhandledException(ctx, e);
    }
  }

  private void unhandledException(SpiContext ctx, Exception e) {
    log.log(WARNING, "Uncaught exception", e);
    defaultHandling(ctx, new HttpResponseException(ErrorCode.INTERNAL_SERVER_ERROR));
  }

  private void defaultHandling(SpiContext ctx, HttpResponseException exception) {

    ctx.status(exception.getStatus());
    if (exception.getStatus() == ErrorCode.REDIRECT.status()) {
      ctx.performRedirect();
    } else if (useJson(ctx)) {
      ctx.contentType(APPLICATION_JSON).write(asJsonContent(exception));
    } else {
      ctx.text(exception.getMessage());
    }
  }

  private String asJsonContent(HttpResponseException e) {
    return "{\"title\": "
        + jsonEscape(e.getMessage())
        + ", "
        + "\"status\": "
        + e.getStatus()
        + "}";
  }

  private String jsonEscape(String message) {
    return message;
  }

  private boolean useJson(SpiContext ctx) {
    final String acceptHeader = ctx.header(HeaderKeys.ACCEPT);
    return (acceptHeader != null && acceptHeader.contains(APPLICATION_JSON)
        || APPLICATION_JSON.equals(ctx.responseHeader(HeaderKeys.CONTENT_TYPE)));
  }
}
