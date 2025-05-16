package io.avaje.jex.core;

import static java.lang.System.Logger.Level.ERROR;

import java.util.Map;

import io.avaje.applog.AppLog;
import io.avaje.jex.http.HttpStatus;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.ExceptionHandler;
import io.avaje.jex.http.HttpResponseException;
import io.avaje.jex.http.InternalServerErrorException;

final class ExceptionManager {

  private static final String APPLICATION_JSON = "application/json";

  private static final System.Logger log = AppLog.getLogger("io.avaje.jex");

  private final Map<Class<?>, ExceptionHandler<?>> handlers;

  ExceptionManager(Map<Class<?>, ExceptionHandler<?>> handlers) {
    this.handlers = handlers;
  }

  @SuppressWarnings("unchecked")
  ExceptionHandler<Exception> find(Class<?> exceptionType) {
    Class<?> type = exceptionType;
    do {
      final var handler = handlers.get(type);
      if (handler != null) {
        return (ExceptionHandler<Exception>) handler;
      }
      type = type.getSuperclass();
    } while (type != null);
    return null;
  }

  void handle(JdkContext ctx, Exception e) {
    final var handler = find(e.getClass());
    if (handler != null) {
      try {
        handler.handle(ctx, e);
      } catch (Exception ex) {
        unhandledException(ctx, ex);
      }
    } else if (e instanceof HttpResponseException ex) {
      defaultHandling(ctx, ex);
    } else {
      unhandledException(ctx, e);
    }
  }

  private void unhandledException(JdkContext ctx, Exception e) {
    log.log(ERROR, "Uncaught exception", e);
    defaultHandling(ctx, new InternalServerErrorException("Internal Server Error"));
  }

  private void defaultHandling(JdkContext ctx, HttpResponseException exception) {
    if (ctx.responseSent()) {
      // if already sent headers, can't send again
      return;
    }

    ctx.status(exception.status());
    var jsonResponse = exception.jsonResponse();
    if (exception.status() == HttpStatus.FOUND_302.status()) {
      ctx.performRedirect();
    } else if (jsonResponse != null) {
      ctx.json(jsonResponse);
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
        + e.status()
        + "}";
  }

  private String jsonEscape(String message) {
    return message;
  }

  private boolean useJson(Context ctx) {
    final String acceptHeader = ctx.header(Constants.ACCEPT);
    return (acceptHeader != null && acceptHeader.contains(APPLICATION_JSON)
        || APPLICATION_JSON.equals(ctx.responseHeader(Constants.CONTENT_TYPE)));
  }
}
