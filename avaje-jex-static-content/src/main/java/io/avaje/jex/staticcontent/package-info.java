/**
 * Static Content API - see {@link io.avaje.jex.staticcontent.StaticContentService}.
 *
 * <pre>{@code
 * var staticContent = StaticContentService.createCP("/public").httpPath("/").directoryIndex("index.html");
 * final Jex.Server app = Jex.create()
 *   .routing(staticContent)
 *   .port(8080)
 *   .start();
 *
 * app.shutdown();
 *
 * }</pre>
 */
package io.avaje.jex.staticcontent;
