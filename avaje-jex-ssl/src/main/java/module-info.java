/**
 * Defines the SSL Plugin API for configuring SSL and mTLS with Jex - see {@link io.avaje.jex.ssl.SslPlugin}.
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
  exports io.avaje.jex.ssl.impl to io.avaje.jex.http3.flupke;

  requires transitive io.avaje.jex;

}
