package io.avaje.jex.core;

import io.avaje.applog.AppLog;
import io.avaje.jex.ErrorHandling;
import io.avaje.jex.ExceptionHandler;
import io.avaje.jex.http.HttpResponseException;
import io.avaje.jex.http.InternalServerErrorResponse;
import io.avaje.jex.http.RedirectResponse;
import io.avaje.jex.spi.HeaderKeys;
import io.avaje.jex.spi.SpiContext;

import java.lang.System.Logger.Level;

class ExceptionManager {

  private static final System.Logger log = AppLog.getLogger("io.avaje.jex");

  private final ErrorHandling errorHandling;

  ExceptionManager(ErrorHandling errorHandling) {
    this.errorHandling = errorHandling;
  }

  void handle(SpiContext ctx, Exception e) {
    final ExceptionHandler<Exception> handler = errorHandling.find(e.getClass());
    if (handler != null) {
      handler.handle(e, ctx);
    } else {
      if (canHandle(e)) {
        defaultHandling(ctx, e);
      } else {
        unhandledException(ctx, e);
      }
    }
  }

  private void unhandledException(SpiContext ctx, Exception e) {
    log.log(Level.WARNING, "Uncaught exception", e);
    defaultHandling(ctx, new InternalServerErrorResponse());
  }

  private boolean canHandle(Exception e) {
    return HttpResponseException.class.isAssignableFrom(e.getClass());
  }

  private boolean isRedirect(Exception e) {
    return RedirectResponse.class.isAssignableFrom(e.getClass());
  }

  private void defaultHandling(SpiContext ctx, Exception exception) {
    final HttpResponseException e = unwrap(exception);
    ctx.status(e.getStatus());
    if (isRedirect(e)) {
      ctx.performRedirect();
    } else if (useJson(ctx)) {
      ctx.contentType("application/json").write(asJsonContent(e));
    } else {
      ctx.text(asTextContent(e));
    }
  }

  private String asTextContent(HttpResponseException e) {
    return e.getMessage();
    // + "\n" details
  }

  private String asJsonContent(HttpResponseException e) {
    return "{\"title\": " + jsonEscape(e.getMessage()) + ", " +
      "\"status\": " + e.getStatus() +
      //+ ", "  "\"type\": " + ", " +
      jsonDetails(e) + "}";
  }

  private String jsonEscape(String message) {
    return message;
  }

  private String jsonDetails(HttpResponseException e) {
    return "";
  }

  private HttpResponseException unwrap(Exception e) {
    return (HttpResponseException) e;
    //(if (e is CompletionException) e.cause else e) as HttpResponseException
  }

  private boolean useJson(SpiContext ctx) {
    final String acceptHeader = ctx.header(HeaderKeys.ACCEPT);
    return (acceptHeader != null && acceptHeader.contains("application/json")
      || "application/json".equals(ctx.responseHeader(HeaderKeys.CONTENT_TYPE)));
  }

}
