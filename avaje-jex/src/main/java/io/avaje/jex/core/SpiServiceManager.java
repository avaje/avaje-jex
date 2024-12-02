package io.avaje.jex.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;

/**
 * Core service methods available to Context implementations.
 */
public sealed interface SpiServiceManager permits CoreServiceManager, CtxServiceManager {

  /**
   * Read and return the type from json request content.
   */
  <T> T jsonRead(Class<T> clazz, InputStream ctx);

  /**
   * Write as json to response content.
   */
  void jsonWrite(Object bean, OutputStream ctx);

  /**
   * Write as json stream to response content.
   */
  <E> void jsonWriteStream(Stream<E> stream, OutputStream ctx);

  /**
   * Write as json stream to response content.
   */
  <E> void jsonWriteStream(Iterator<E> iterator, OutputStream ctx);

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
  void handleException(JdkContext ctx, Exception e);

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

  /**
   * Parse and return the content as url encoded parameters.
   */
  Map<String, List<String>> parseParamMap(String body, String charset);
}
