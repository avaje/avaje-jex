package io.avaje.jex.spi;

import io.avaje.jex.Jex;

/**
 * A plugin that can register things like routes, exception handlers etc.
 */
public interface JexPlugin {

  /**
   * Register the plugin features with jex.
   */
  void apply(Jex jex);
}
