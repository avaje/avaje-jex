package io.avaje.jex;

import io.avaje.jex.http.Context;

import java.util.Map;

/**
 * A parser for URL paths.
 * <p>
 * It is recommended to use the default, which is compliant with the specification.
 *
 * @since 4.0
 */
public abstract class PathParser {

  /**
   * Test if the provided URL matches this PathParser. This is invoked every routing request.
   *
   * @param url The URL to test
   * @return If it matches
   */
  public abstract boolean matches(final String url);

  /**
   * Extract all the path params out of the provided URI. This is invoked the once the first time
   *    a {@link Context#pathParamMap()} or any other path param method is used.
   *
   * @param uri The URI to parse
   * @return The path parameters
   */
  public abstract Map<String, String> extractPathParams(String uri);

  /**
   * Return the raw path that was parsed (match path).
   */
  public abstract String raw();

  /**
   * Return the number of path segments.
   */
  public abstract int segmentCount();

  /**
   * Return true if one of the segments is wildcard or slash accepting.
   */
  public abstract boolean multiSlash();

  /**
   * Return true if all path segments are literal.
   */
  public abstract boolean literal();
}
