package io.avaje.jex.spi;

import java.util.Iterator;

/**
 * Service used to convert request/response bodies to beans.
 */
public non-sealed interface JsonService extends JexExtension {

  /**
   * Read the request body as a bean and return the bean.
   */
  <T> T jsonRead(Class<T> type, SpiContext ctx);

  /**
   * Write the bean as JSON response content.
   */
  void jsonWrite(Object bean, SpiContext ctx);

  /**
   * Write the beans as {@literal x-json-stream } JSON with new line delimiter.
   */
  <E> void jsonWriteStream(Iterator<E> stream, SpiContext ctx);

}
