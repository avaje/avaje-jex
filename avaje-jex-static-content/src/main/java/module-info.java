import io.avaje.jex.spi.JexExtension;

/**
 * Defines the Jex HTTP server API, for running a minimal HTTP server.
 *
 * <pre>{@code
 * final Jex.Server app = Jex.create()
 *   .routing(routing -> routing
 *     .get("/", ctx -> ctx.text("hello world"))
 *     .get("/one", ctx -> ctx.text("one"))
 *   .port(8080)
 *   .start();
 *
 * app.shutdown();
 *
 * }</pre>
 *
 *
 */
module io.avaje.jex.staticcontent {

  exports io.avaje.jex.staticcontent;

  requires transitive io.avaje.jex;

}
