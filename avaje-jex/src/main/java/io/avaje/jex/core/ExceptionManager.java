package io.avaje.jex.core;

import io.avaje.jex.Context;
import io.avaje.jex.ErrorHandling;
import io.avaje.jex.ExceptionHandler;
import io.avaje.jex.http.HttpResponseException;
import io.avaje.jex.http.InternalServerErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExceptionManager {

  private static final Logger log = LoggerFactory.getLogger(ExceptionManager.class);

  private final ErrorHandling errorHandling;

  ExceptionManager(ErrorHandling errorHandling) {
    this.errorHandling = errorHandling;
  }

  void handle(Context ctx, Exception e) {
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

  private void unhandledException(Context ctx, Exception e) {
    log.warn("Uncaught exception", e);
    defaultHandling(ctx, new InternalServerErrorResponse());
  }

  private boolean canHandle(Exception e) {
    return HttpResponseException.class.isAssignableFrom(e.getClass());
  }

  private void defaultHandling(Context ctx, Exception exception) {
    final HttpResponseException e = unwrap(exception);
    ctx.status(e.getStatus());
    if (useJson(ctx)) {
      ctx.text(asJsonContent(e)).contentType("application/json");
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

  private boolean useJson(Context ctx) {
    final String acceptHeader = ctx.header(HeaderKeys.ACCEPT);
    return (acceptHeader != null && acceptHeader.contains("application/json")
      || "application/json".equals(ctx.contentType()));
  }


}
