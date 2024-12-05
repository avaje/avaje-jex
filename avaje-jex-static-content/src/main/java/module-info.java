/**
 * Defines the Static Content API for serving static resources with Jex - see {@link io.avaje.jex.staticcontent.StaticContentService}.
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
module io.avaje.jex.staticcontent {

  exports io.avaje.jex.staticcontent;

  requires transitive io.avaje.jex;

}
