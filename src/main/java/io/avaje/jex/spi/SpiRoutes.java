package io.avaje.jex.spi;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;

import java.util.Map;

/**
 * Route matching and filter handling.
 */
public interface SpiRoutes {

  /**
   * Find the matching handler entry given the type and request URI.
   */
  Entry match(Routing.Type type, String pathInfo);

  /**
   * Execute all appropriate before filters for the given request URI.
   */
  void before(String pathInfo, Context ctx);

  /**
   * Execute all appropriate after filters for the given request URI.
   */
  void after(String pathInfo, Context ctx);

  /**
   * A route entry.
   */
  interface Entry {

    /**
     * Return true if it matches the request URI.
     */
    boolean matches(String requestUri);

    /**
     * Handle the request.
     */
    void handle(Context ctx);

    /**
     * Return the path parameter map given the uri.
     */
    Map<String, String> pathParams(String uri);

    /**
     * Return the raw path expression.
     */
    String rawPath();

    /**
     * Return the segment count.
     */
    int getSegmentCount();
  }
}
