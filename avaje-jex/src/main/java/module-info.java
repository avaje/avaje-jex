import io.avaje.jex.spi.JexExtension;

/**
 * Defines the Jex HTTP server API, for running a minimal HTTP server.
 *
 * <pre>{@code
 * final Jex.Server app = Jex.create()
 *   .get("/", ctx -> ctx.text("hello world"))
 *   .get("/one", ctx -> ctx.text("one"))
 *   .port(8080)
 *   .start();
 *
 * app.shutdown();
 *
 * }</pre>
 *
 * @uses JexExtension
 *
 */
module io.avaje.jex {

  exports io.avaje.jex;
  exports io.avaje.jex.compression;
  exports io.avaje.jex.http;
  exports io.avaje.jex.http.sse;
  exports io.avaje.jex.core.json;
  exports io.avaje.jex.security;
  exports io.avaje.jex.spi;

  requires transitive java.net.http;
  requires transitive jdk.httpserver;
  requires static com.fasterxml.jackson.core;
  requires static com.fasterxml.jackson.databind;
  requires static io.avaje.jsonb;
  requires static io.avaje.inject;
  requires static io.avaje.config;
  requires static io.avaje.spi;

  uses JexExtension;
}
