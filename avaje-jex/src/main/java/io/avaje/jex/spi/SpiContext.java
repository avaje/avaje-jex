package io.avaje.jex.spi;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface SpiContext extends Context {

  /**
   * Return the response outputStream to write content to.
   */
  OutputStream outputStream();

  /**
   * Return the request inputStream to read content from.
   */
  InputStream inputStream() throws IOException;

  /**
   * Set to indicate BEFORE, Handler AFTER modes of the request.
   */
  void setMode(Routing.Type type);
}
