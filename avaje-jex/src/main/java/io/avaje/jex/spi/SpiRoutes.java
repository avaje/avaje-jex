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
  void before(String pathInfo, SpiContext ctx);

  /**
   * Execute all appropriate after filters for the given request URI.
   */
  void after(String pathInfo, SpiContext ctx);

  /**
   * Increment active request count for no route match.
   */
  void inc();

  /**
   * Decrement active request count for no route match.
   */
  void dec();

  /**
   * Return the active request count.
   */
  long activeRequests();

  /**
   * Wait for no active requests.
   */
  void waitForIdle(long maxSeconds);

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
    String matchPath();

    /**
     * Return the segment count.
     */
    int segmentCount();

    /**
     * Return true if one of the segments is the wildcard match or accepting slashes.
     */
    boolean multiSlash();

    /**
     * Return true if all segments are literal.
     */
    boolean literal();

    /**
     * Increment active request count for the route.
     */
    void inc();

    /**
     * Decrement active request count for the route.
     */
    void dec();

    /**
     * Return the active request count for the route.
     */
    long activeRequests();
  }
}
