package io.avaje.jex.spi;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Core service methods available to Context implementations.
 */
public interface SpiServiceManager {

  /**
   * Read and return the type from json request content.
   */
  <T> T jsonRead(Class<T> clazz, SpiContext ctx);

  /**
   * Write as json to response content.
   */
  void jsonWrite(Object bean, SpiContext ctx);

  /**
   * Write as json stream to response content.
   */
  <E> void jsonWriteStream(Stream<E> stream, SpiContext ctx);

  /**
   * Write as json stream to response content.
   */
  <E> void jsonWriteStream(Iterator<E> iterator, SpiContext ctx);

  /**
   * Maybe close if iterator is a AutoClosable.
   */
  void maybeClose(Object iterator);

  /**
   * Return the routing type given the http method.
   */
  Routing.Type lookupRoutingType(String method);

  /**
   * Handle the exception.
   */
  void handleException(Context ctx, Exception e);

  /**
   * Render using template manager.
   */
  void render(Context ctx, String name, Map<String, Object> model);

  /**
   * Return the character set of the request.
   */
  String requestCharset(Context ctx);

  /**
   * Parse and return the body as form parameters.
   */
  Map<String, List<String>> formParamMap(Context ctx, String charset);

}
