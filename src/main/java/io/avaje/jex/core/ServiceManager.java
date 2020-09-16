package io.avaje.jex.core;

import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.SpiContext;

public class ServiceManager {

  private final JsonService jsonService;

  public ServiceManager(JsonService jsonService) {
    this.jsonService = jsonService;
  }

  public <T> T bodyAsClass(Class<T> clazz, SpiContext ctx) {
    return jsonService.jsonRead(clazz, ctx);
  }

  public void json(Object bean, SpiContext ctx) {
    jsonService.jsonWrite(bean, ctx);
  }
}
