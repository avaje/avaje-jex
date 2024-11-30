package io.avaje.jex.spi;

import io.avaje.jex.Jex;

/**
 * A plugin that can register things like routes, exception handlers and configure the current Jex
 * instance.
 *
 * @see JexExtension for SPI registration details.
 */
@FunctionalInterface
public non-sealed interface JexPlugin extends JexExtension {

  /** Register the plugin features with jex. */
  void apply(Jex jex);
}
