/**
 * Avaje Jex API - see {@link io.avaje.jex.staticcontent.staticcontent.Jex}.
 *
 * <pre>{@code
 * var staticContent = StaticContentSupport.create().resource("/public").directoryIndex("index.html");
 * final Jex.Server app = Jex.create()
 *   .routing(staticContent.createService())
 *   .port(8080)
 *   .start();
 *
 * app.shutdown();
 *
 * }</pre>
 */
package io.avaje.jex.staticcontent;
