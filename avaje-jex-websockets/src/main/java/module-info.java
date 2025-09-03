/**
 * Defines the Static Content API for serving static resources with Jex - see {@link io.avaje.jex.staticcontent.StaticContent}.
 *
 * <pre>{@code
 * var staticContent = StaticContentService.createCP("/public").httpPath("/").directoryIndex("index.html");
 * final Jex.Server app = Jex.create()
 *   .plugin(staticContent)
 *   .port(8080)
 *   .start();
 *
 * app.shutdown();
 *
 * }</pre>
 */
module io.avaje.jex.websocket {

  exports io.avaje.jex.websocket;
  exports io.avaje.jex.websocket.exception;

  requires transitive io.avaje.jex;
  requires static java.logging;

}
