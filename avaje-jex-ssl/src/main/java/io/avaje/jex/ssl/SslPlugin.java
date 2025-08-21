package io.avaje.jex.ssl;

import java.util.function.Consumer;

import io.avaje.jex.spi.JexPlugin;

/**
 * Plugin that Configures Jex with SSL and mTLS.
 *
 * <pre>{@code
 * var sslPlugin =
 *     SslPlugin.create(
 *         config ->
 *             config.keystoreFromClasspath("path", "password"));
 *
 * Jex.create()
 *  .plugin(sslPlugin)
 *  .port(8080)
 *  .start();
 *
 * }</pre>
 */
public sealed interface SslPlugin extends JexPlugin permits DSslPlugin {

  /**
   * Configure SSL using an existing SSL Context
   *
   * @param consumer consumer that
   */
  static SslPlugin create(Consumer<SslConfig> consumer) {
    return new DSslPlugin(consumer);
  }
}
