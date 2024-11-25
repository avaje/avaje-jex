package io.avaje.jex;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.io.InputStream;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.sun.net.httpserver.HttpExchange;

import io.avaje.jex.security.BasicAuthCredentials;
import io.avaje.jex.security.Role;
import io.avaje.jex.spi.HeaderKeys;

/**
 * Provides access to functions for handling the request and response.
 */
public interface Context {

  /**
   * Return the matched path as a raw expression.
   */
  String matchedPath();

  /**
   * Sets an attribute on the request.
   * <p>
   * Attributes are available to other handlers in the request lifecycle
   */
  Context attribute(String key, Object value);

  /**
   * Get the specified attribute from the request.
   */
  <T> T attribute(String key);

  /**
   * Return a request cookie by name, or null.
   */
  String cookie(String name);

  /**
   * Returns a map with all the cookie keys and values on the request.
   */
  Map<String, String> cookieMap();

  /**
   * Sets a cookie with name, value with unlimited age.
   */
  Context cookie(String name, String value);

  /**
   * Sets a cookie with name, value, and max-age.
   */
  Context cookie(String name, String value, int maxAge);

  /**
   * Sets a Cookie.
   */
  Context cookie(Cookie cookie);

  /**
   * Remove a cookie by name.
   */
  Context removeCookie(String name);

  /**
   * Remove a cookie by name and path.
   */
  Context removeCookie(String name, String path);

  /**
   * Redirect to the specified location using 302 status code.
   */
  void redirect(String location);

  /**
   * Redirect to the location specifying the response status code.
   */
  void redirect(String location, int httpStatusCode);

  /** Roles attached to this route */
  Set<Role> routeRoles();

  /**
   * Return the request body as bytes.
   */
  byte[] bodyAsBytes();

  /***
   * Return the request body as bean.
   *
   * @param beanType The bean type
   */
  <T> T bodyAsClass(Class<T> beanType);

  /**
   * Return the request body as String.
   */
  String body();

  /**
   * Return the request content length.
   */
  long contentLength();

  /**
   * Return the request content type.
   */
  String contentType();

  /**
   * Set the response content type.
   */
  Context contentType(String contentType);

  /**
   * Return all the path parameters as a map.
   */
  Map<String, String> pathParamMap();

  /**
   * Return the path parameter.
   *
   * @param name The path parameter name.
   */
  String pathParam(String name);

  /**
   * Return the first query parameter value.
   *
   * @param name The query parameter name
   */
  String queryParam(String name);

  /**
   * Return the first query parameter value or the default value if it does not exist.
   *
   * @param name The query parameter name
   */
  default String queryParam(String name, String defaultValue) {
    String val = queryParam(name);
    return val != null ? val : defaultValue;
  }

  /**
   * Return all the query parameters for the given parameter name.
   */
  List<String> queryParams(String name);

  /**
   * Return all the query parameters as a map.
   * <p>
   * Note this returns the first value for any given key if that key has multiple values.
   */
  Map<String, String> queryParamMap();

  /**
   * Return the request query string, or null.
   */
  String queryString();

  /**
   * Return the first form param value for the specified key or null.
   */
  default String formParam(String key) {
    return formParam(key, null);
  }

  /**
   * Return the first form param value for the specified key or the default value.
   */
  default String formParam(String key, String defaultValue) {
    final List<String> values = formParamMap().get(key);
    return values == null || values.isEmpty() ? defaultValue : values.get(0);
  }

  /**
   * Return the form params for the specified key, or empty list.
   */
  default List<String> formParams(String key) {
    final List<String> values = formParamMap().get(key);
    return values != null ? values : emptyList();
  }

  /**
   * Returns a map with all the form param keys and values.
   */
  Map<String, List<String>> formParamMap();

  /**
   * Return the underlying JDK {@link HttpExchange} object backing the context
   */
  HttpExchange jdkExchange();

  /**
   * Return the request scheme.
   */
  String scheme();

  /**
   * Return the request url.
   */
  String url();

  /**
   * Return the full request url, including query string (if present)
   */
  default String fullUrl() {
    final String url = url();
    final String qs = queryString();
    return qs == null ? url : url + "?" + qs;
  }

  /**
   * Return the request context path.
   */
  String contextPath();

  /**
   * Return the request user agent, or null.
   */
  default String userAgent() {
    return header(HeaderKeys.USER_AGENT);
  }

  /**
   * Set the status code on the response.
   */
  Context status(int statusCode);

  /**
   * Return the current response status.
   */
  int status();

  /**
   * Write plain text content to the response.
   */
  Context text(String content);

  /**
   * Write html content to the response.
   */
  Context html(String content);

  /**
   * Set the response body as JSON for the given bean.
   */
  Context json(Object bean);

  /**
   * Write the stream as a JSON stream with new line delimiters
   * {@literal application/x-json-stream}.
   *
   * @param stream The stream of beans to write as json
   */
  <E> Context jsonStream(Stream<E> stream);

  /**
   * Write the stream as a JSON stream with new line delimiters
   * {@literal application/x-json-stream}.
   *
   * @param iterator The iterator of beans to write as json
   */
  <E> Context jsonStream(Iterator<E> iterator);

  /**
   * Write raw content to the response.
   */
  Context write(String content);

  /**
   * Write raw bytes to the response.
   */
  Context write(byte[] bytes);

  /**
   * Write raw inputStream to the response.
   */
  Context write(InputStream is);

  /**
   * Render a template typically as html.
   *
   * @param name The template name
   */
  default Context render(String name) {
    return render(name, emptyMap());
  }

  /**
   * Render a template typically as html with the given model.
   *
   * @param name  The template name
   * @param model The model used with the template
   */
  Context render(String name, Map<String, Object> model);

  /**
   * Return all the request headers as a map.
   */
  Map<String, String> headerMap();

  /**
   * Return the request header.
   *
   * @param key The header key
   */
  String header(String key);

  /**
   * Set the response header.
   *
   * @param key   The header key
   * @param value The header value
   */
  Context header(String key, String value);

  /** Set the response headers using the provided map. */
  default Context headers(Map<String, String> headers) {
    headers.forEach(this::header);
    return this;
  }

  /**
   * Return the response header.
   */
  String responseHeader(String key);

  /**
   * Returns the request host, or null.
   */
  String host();

  /**
   * Returns the request IP.
   */
  String ip();

  /**
   * Returns the request method.
   */
  String method();

  /**
   * Return the request path.
   */
  String path();

  /**
   * Return the request port.
   */
  int port();

  /**
   * Return the request protocol.
   */
  String protocol();

  /**
   * Gets basic-auth credentials from the request, or throws.
   *
   * <p>Returns a wrapper object containing the Base64 decoded username
   * and password from the Authorization header, or null if basic-auth is not properly configured
   */
  BasicAuthCredentials basicAuthCredentials();

  interface Cookie {

    /**
     * Return an expired cookie given the name.
     *
     * @param name The name of the cookie.
     * @return The expired cookie
     */
    static Cookie expired(String name) {
      return DCookie.expired(name);
    }

    /**
     * Return a new cookie given the name and value.
     *
     * @param name  The name of the cookie
     * @param value The cookie value
     * @return The new cookie
     */
    static Cookie of(String name, String value) {
      return DCookie.of(name, value);
    }

    /**
     * Return the name.
     */
    String name();

    /**
     * Return the value.
     */
    String value();

    /**
     * Return the domain.
     */
    String domain();

    /**
     * Set the domain.
     */
    Cookie domain(String domain);

    /**
     * Return the max age.
     */
    Duration maxAge();

    /**
     * Set the max age.
     */
    Cookie maxAge(Duration maxAge);

    /**
     * Return the cookie expiration.
     */
    ZonedDateTime expires();

    /**
     * Set when the cookie expires.
     */
    Cookie expires(ZonedDateTime expires);

    /**
     * Return the path.
     */
    String path();

    /**
     * Set the path.
     */
    Cookie path(String path);

    /**
     * Return the secure attribute of the cookie.
     */
    boolean secure();

    /**
     * Set the secure attribute of the cookie.
     */
    Cookie secure(boolean secure);

    /**
     * Return the httpOnly attribute of the cookie.
     */
    boolean httpOnly();

    /**
     * Set the httpOnly attribute of the cookie.
     */
    Cookie httpOnly(boolean httpOnly);

    /**
     * Returns content of the cookie as a 'Set-Cookie:' header value specified
     * by <a href="https://tools.ietf.org/html/rfc6265">RFC6265</a>.
     */
    @Override
    String toString();

  }

}
