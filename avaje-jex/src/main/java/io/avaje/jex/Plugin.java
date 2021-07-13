package io.avaje.jex;

/**
 * A plugin that can register things like routes, exception handlers etc.
 */
public interface Plugin {

  /**
   * Register the plugin features with jex.
   */
  void apply(Jex jex);
}
