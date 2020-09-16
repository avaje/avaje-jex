package io.avaje.jex.spi;

import io.avaje.jex.Context;

import java.io.OutputStream;

public interface SpiContext extends Context {
  OutputStream outputStream();
}
