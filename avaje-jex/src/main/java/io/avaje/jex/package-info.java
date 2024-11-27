/**
 * Avaje Jex API - see {@link io.avaje.jex.Jex}.
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
 */
package io.avaje.jex;
