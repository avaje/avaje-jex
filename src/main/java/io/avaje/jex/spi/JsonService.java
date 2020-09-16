package io.avaje.jex.spi;

public interface JsonService {

  <T> T jsonRead(Class<T> clazz, SpiContext ctx);

  void jsonWrite(Object bean, SpiContext ctx);
}
