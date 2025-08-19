/**
 * Defines the SSL Plugin API for configuring SSL and mTLS with Jex - see {@link io.avaje.jex.ssl.StaticContent}.
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
module io.avaje.jex.ssl {

  exports io.avaje.jex.ssl;

  requires transitive io.avaje.jex;

}
