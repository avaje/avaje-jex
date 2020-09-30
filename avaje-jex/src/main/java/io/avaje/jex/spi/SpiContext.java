package io.avaje.jex.spi;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Extension to Context for processing the request.
 */
public interface SpiContext extends Context {

  /**
   * Return the response outputStream to write content to.
   */
  OutputStream outputStream();

  /**
   * Return the request inputStream to read content from.
   */
  InputStream inputStream();

  /**
   * Set to indicate BEFORE, Handler and AFTER modes of the request.
   */
  void setMode(Routing.Type type);
}
