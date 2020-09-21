package io.avaje.jex.core;

import io.avaje.jex.Context;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.SpiContext;

public class ServiceManager {

  private final JsonService jsonService;

  private final ExceptionHandler exceptionHandler = new ExceptionHandler();

  public ServiceManager(JsonService jsonService) {
    this.jsonService = jsonService;
  }

  public <T> T jsonRead(Class<T> clazz, SpiContext ctx) {
    return jsonService.jsonRead(clazz, ctx);
  }

  public void jsonWrite(Object bean, SpiContext ctx) {
    jsonService.jsonWrite(bean, ctx);
  }

  public void handleException(Context ctx, Exception e) {
    exceptionHandler.handle(ctx, e);
  }
}
