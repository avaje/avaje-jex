package io.avaje.jex.core;

import io.avaje.jex.Context;
import io.avaje.jex.ErrorHandling;
import io.avaje.jex.ExceptionHandler;

public class ExceptionManager {

  private final ErrorHandling errorHandling;

  public ExceptionManager(ErrorHandling errorHandling) {
    this.errorHandling = errorHandling;
  }

  public void handle(Context ctx, Exception e) {
    final ExceptionHandler<Exception> handler = errorHandling.find(e.getClass());
    if (handler != null) {
      handler.handle(e, ctx);
    }
  }
}
