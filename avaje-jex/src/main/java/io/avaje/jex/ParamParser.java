package io.avaje.jex;

import io.avaje.jex.http.Context;
import io.avaje.jex.routes.DPathParser;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Parser for the various parameter types (path, query, body).
 * <p>
 * When overriding this class, you may use the helper methods
 * {@link #decodeListAsRfc3986(String, Charset)},
 * {@link #defaultPathParser(String, boolean)}, and/or
 * {@link #decodeListAsUrlEncoded(String, Charset)} to assist you.
 * <p>
 * The default implementation acts as below:
 * <ul>
 *   <li>Query params are parsed as {@code application/x-www-form-urlencoded}</li>
 *   <li>Path params are parsed as per RFC 3986</li>
 *   <li>Bodies are parsed depending on the incoming {@code Content-Type}:
 *    <ul>
 *      <li>If it's {@code application/x-www-form-urlencoded}, then parse as that</li>
 *      <li>If it's {@code multipart/form-data}, then you should use {@code avaje-jex-file-upload}</li>
 *      <li>Otherwise, you must provide another parser (e.g. {@code application/json} using {@code avaje-jsonb}),
 *      otherwise your users will receive a {@code 415 UNSUPPORTED MEDIA TYPE)</li>
 *    </ul>
 *   </li>
 * </ul>
 *
 * @since 4.0
 */
public abstract class ParamParser {

  /**
   * Decode the query params. This is invoked the first time per-request
   *    when {@link Context#queryParamMap()} or any other query param method is used.
   *
   * @param ctx The context of the request
   * @param charset The charset to use
   * @return The parsed query params
   */
  public abstract Map<String, List<String>> decodeQueryParams(final Context ctx, final Charset charset);

  /**
   * Decode a request body. This is invoked the first time per-request
   *    when {@link Context#formParamMap()} or any other form param method is used.
   *
   * @param ctx The context of the request
   * @param charset The charset to use
   * @return The parsed body
   */
  public abstract Map<String, List<String>> decodeBody(final Context ctx, final Charset charset);

  /**
   * Create a path parser. These are constructed once, at start-up,
   *    with some methods within them being invoked every request.
   *
   * @param path The entire path to parse
   * @param routeEntry The route entry
   * @param ignoreTrailingSlashes If trailing slashes should be ignored
   * @return The PathParser instance for the given path
   */
  public abstract PathParser createPathParser(final String path, final Routing.Entry routeEntry, final boolean ignoreTrailingSlashes);

  /**
   * Create a default {@link PathParser} implementation that complies with the RFC 3986 spec.
   *
   * @param path The path to parse
   * @param ignoreTrailingSlashes If trailing slashes should be ignored
   * @return The default path parser
   */
  public final PathParser defaultPathParser(final String path, final boolean ignoreTrailingSlashes) {
    return new DPathParser(path, ignoreTrailingSlashes);
  }

  /**
   * Provide decoding based on RFC 3986, into a key -> list of values.
   *
   * @param input The input to try and parse
   * @param charset The character set to parse as
   * @return The decoded input
   */
  public final Map<String, List<String>> decodeListAsRfc3986(final String input, final Charset charset) {
    return parseToMapList(input, charset, true);
  }

  /**
   * Provide decoding based on {@code application/x-www-form-urlencoded}, into a key -> list of values.
   * <p>
   * This is usually used for query parameters and form bodies
   *
   * @param input The input to try and parse
   * @param charset The character set to parse as
   * @return The decoded input
   */
  public final Map<String, List<String>> decodeListAsUrlEncoded(final String input, final Charset charset) {
    return parseToMapList(input, charset, false);
  }

  /**
   * Internal default map parsing
   *
   * @param input The input as a string to be decoded
   * @param charset The charset to use
   * @param useRfc3986 If we should use RFC 3986 parsing (true), or standard form encoding (false)
   * @return A map of key, to a list of its values
   */
  private static Map<String, List<String>> parseToMapList(final String input, final Charset charset, final boolean useRfc3986) {
    if (input == null || input.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<String, List<String>> map = new LinkedHashMap<>();
    int start = 0;
    int len = input.length();

    while (start < len) {
      int amp = input.indexOf('&', start);
      int end = amp == -1 ? len : amp;
      int eq = input.indexOf('=', start);

      String key, val;
      if (eq == -1 || eq > end) {
        key = decode(input.substring(start, end), charset, useRfc3986);
        val = "";
      } else {
        key = decode(input.substring(start, eq), charset, useRfc3986);
        val = decode(input.substring(eq + 1, end), charset, useRfc3986);
      }
      map.computeIfAbsent(key, s -> new ArrayList<>()).add(val);
      start = end + 1;
    }
    return map;
  }

  /**
   * Internal utility to decode a string
   *
   * @param s The string to decode
   * @param charset The charset to decode as
   * @param useRfc3986 If we should use RFC 3986 parsing (true), or standard form encoding (false)
   * @return The decoded string
   */
  private static String decode(String s, Charset charset, boolean useRfc3986) {
    return useRfc3986 ? rfc3986decode(s, charset) : URLDecoder.decode(s, charset);
  }

  /**
   * Internal utility to help decode using RFC 3986
   *
   * @param s The string to decode
   * @param charset The character set to decode in
   * @return The RFC 3986 decoded version
   */
  private static String rfc3986decode(final String s, final Charset charset) {
    if (s.indexOf('+') == -1) {
      return URLDecoder.decode(s, charset);
    }
    return URLDecoder.decode(s.replace("+", "%2B"), charset).replace("%2B", "+");
  }
}
