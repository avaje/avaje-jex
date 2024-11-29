package io.avaje.jex.routes;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.avaje.jex.Context;
import io.avaje.jex.ExchangeHandler;
import io.avaje.jex.HttpFilter;
import io.avaje.jex.Routing;
import io.avaje.jex.security.Role;

/**
 * Route matching and filter handling.
 */
public sealed interface SpiRoutes permits Routes {

  /**
   * Find the matching handler entry given the type and request URI.
   */
  Entry match(Routing.Type type, String pathInfo);

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
   * Get filters
   */
  List<HttpFilter> filters();

  /**
   * A route entry.
   */
  interface Entry {

    /**
     * Return true if it matches the request URI.
     */
    boolean matches(String requestUri);

    /**
     * Handler for the request.
     */
    ExchangeHandler handler();

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

    /** Return the authentication roles for the route. */
    Set<Role> roles();
  }

}
