package io.avaje.jex.spi;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;

import java.io.OutputStream;

public interface SpiContext extends Context {

  /**
   * Return the response outputStream to write content to.
   */
  OutputStream outputStream();

  /**
   * Set to indicate BEFORE, Handler AFTER modes of the request.
   */
  void setMode(Routing.Type type);
}
