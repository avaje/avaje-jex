/**
 * Defines the Static Content API for serving static resources with Jex - see {@link io.avaje.jex.staticcontent.StaticContentSupport}.
 *
 * <pre>{@code
 * var staticContent = StaticContentSupport.createCP().resource("/public").directoryIndex("index.html");
 * final Jex.Server app = Jex.create()
 *   .routing(staticContent.createService())
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
