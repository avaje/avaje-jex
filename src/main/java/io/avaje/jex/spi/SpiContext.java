package io.avaje.jex.spi;

import io.avaje.jex.Context;

import java.io.OutputStream;

public interface SpiContext extends Context {

  /**
   * Return the response outputStream to write content to.
   */
  OutputStream outputStream();

}
