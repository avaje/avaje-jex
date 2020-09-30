package io.avaje.jex.spi;

/**
 * Service used to convert request/response bodies to beans.
 */
public interface JsonService {

  /**
   * Read the request body as a bean and return the bean.
   */
  <T> T jsonRead(Class<T> type, SpiContext ctx);

  /**
   * Write the bean as JSON response content.
   */
  void jsonWrite(Object bean, SpiContext ctx);
}
