/**
 * Static Content API - see {@link io.avaje.jex.staticcontent.StaticContentService}.
 *
 * <pre>{@code
 * var staticContent = StaticContentService.createCP().resource("/public").directoryIndex("index.html");
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
