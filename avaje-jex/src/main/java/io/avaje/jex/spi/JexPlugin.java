package io.avaje.jex.spi;

import io.avaje.jex.Jex;

/**
 * A plugin that can register things like routes, exception handlers etc.
 */
@FunctionalInterface
public non-sealed interface JexPlugin extends JexExtension{

  /**
   * Register the plugin features with jex.
   */
  void apply(Jex jex);
}
