package io.avaje.jex.http;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.net.ssl.SSLSession;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import io.avaje.jex.core.Constants;
import io.avaje.jex.security.BasicAuthCredentials;
import io.avaje.jex.security.Role;

/** Provides access to functions for handling the request and response. */
public interface Context {

  /**
   * Gets the attribute with the specified key from the request.
   *
   * @param <T> The type of the attribute.
   * @param key The attribute key.
   * @return The attribute value, or null if not found.
   */
  <T> T attribute(String key);

  /**
   * Sets an attribute on the request, accessible to other handlers in the request lifecycle.
   *
   * @param key The attribute key.
   * @param value The attribute value.
   */
  Context attribute(String key, Object value);

  /**
   * Gets basic-auth credentials from the request.
   *
   * @return The Base64 decoded username and password from the
   * Authorization header, or null if no header is sent
   *
   * @throws IllegalStateException if the Authorization header is malformed
   */
  BasicAuthCredentials basicAuthCredentials();

  /** Return the request body as String. */
  String body();

  /**
   * Returns the request body as a byte array.
   *
   * @return The request body as a byte array.
   */
  byte[] bodyAsBytes();

  /***
   * Return the request body as bean.
   *
   * @param beanType The bean type
   */
  <T> T bodyAsClass(Class<T> beanType);

  /**
   * Returns the request body as an input stream.
   *
   * @return The request body as an input stream.
   */
  InputStream bodyAsInputStream();

  /***
   * Return the request body as bean.
   *
   * @param beanType The bean type
   */
  <T> T bodyAsType(Type beanType);

  /** Return the request content length. */
  long contentLength();

  /** Return the request content type. */
  String contentType();

  /** Set the response content type. */
  default Context contentType(ContentType contentType) {
    return contentType(contentType.contentType());
  }

  /** Set the response content type. */
  Context contentType(String contentType);

  /** Return the request context path. */
  String contextPath();

  /**
   * Sets a cookie using the provided {@link Cookie} object.
   *
   * @param cookie The cookie object to set.
   */
  Context cookie(Cookie cookie);

  /**
   * Returns the value of a cookie with the specified name from the request.
   *
   * @param name The name of the cookie.
   * @return The value of the cookie, or null if the cookie is not found.
   */
  String cookie(String name);

  /**
   * Sets a cookie with the specified name and value, with no expiration date.
   *
   * @param name The name of the cookie.
   * @param value The value of the cookie.
   */
  Context cookie(String name, String value);

  /**
   * Sets a cookie with the specified name, value, and maximum age in seconds.
   *
   * @param name The name of the cookie.
   * @param value The value of the cookie.
   * @param maxAge The maximum age of the cookie in seconds.
   */
  Context cookie(String name, String value, int maxAge);

  /**
   * Returns a map containing all the cookie names and their corresponding values from the request.
   *
   * @return A map of cookie names to their values.
   */
  Map<String, String> cookieMap();

  /** Return the underlying JDK {@link HttpExchange} object backing the context */
  HttpExchange exchange();

  /** Return the first form param value for the specified key or null. */
  default String formParam(String key) {
    return formParam(key, null);
  }

  /** Return the first form param value for the specified key or the default value. */
  default String formParam(String key, String defaultValue) {
    final List<String> values = formParamMap().get(key);
    return values == null || values.isEmpty() ? defaultValue : values.get(0);
  }

  /** Returns a map with all the form param keys and values. */
  Map<String, List<String>> formParamMap();

  /** Return the form params for the specified key, or empty list. */
  default List<String> formParams(String key) {
    final List<String> values = formParamMap().get(key);
    return values != null ? values : emptyList();
  }

  /** Return the full request url, including query string (if present) */
  default String fullUrl() {
    return scheme() + "://" + host() + uri().toString();
  }

  /**
   * Return the request header.
   *
   * @param key The header key
   */
  String header(String key);

  /**
   * Set the response header.
   *
   * @param key The header key
   * @param value The header value
   */
  Context header(String key, List<String> value);

  /**
   * Set the response header.
   *
   * @param key The header key
   * @param value The header value
   */
  Context header(String key, String value);

  /**
   * Return all the request headers as a map.
   *
   * @return all the headers as a single value Map
   */
  Map<String, String> headerMap();

  /**
   * Sets the response headers using the provided map.
   *
   * @param headers A map containing the header names as keys and their corresponding values as
   *     lists.
   * @return The updated context object.
   */
  Context headerMap(Map<String, List<String>> headers);

  /**
   * Return underlying request headers.
   *
   * @return the request headers
   */
  Headers headers();

  /** Add the response headers using the provided map. */
  default Context headers(Map<String, String> headers) {
    headers.forEach(this::header);
    return this;
  }

  /**
   * Returns the host name of the request.
   *
   * @return The host name of the request, or null if not available.
   */
  String host();

  /** Write html content to the response. */
  void html(String content);

  /**
   * Returns the IP address of the client making the request.
   *
   * @return The IP address of the client.
   */
  String ip();

  /**
   * Set the content type as application/json and write the response.
   *
   * @param bean the object to serialize and write
   */
  void json(Object bean);

  /**
   * Write the stream as a JSON stream with new line delimiters {@literal
   * application/x-json-stream}.
   *
   * @param iterator The iterator of beans to write as json
   */
  <E> void jsonStream(Iterator<E> iterator);

  /**
   * Write the stream as a JSON stream with new line delimiters {@literal
   * application/x-json-stream}.
   *
   * @param stream The stream of beans to write as json
   */
  <E> void jsonStream(Stream<E> stream);

  /**
   * Returns the matched path as a raw expression, without any parameter substitution.
   *
   * @return The matched path as a raw string.
   */
  String matchedPath();

  /**
   * Returns the HTTP method used in the request (e.g., GET, POST, PUT, DELETE).
   *
   * @return The HTTP method of the request.
   */
  String method();

  /**
   * Return the outputStream to write content. It is expected that
   * the {@link #contentType(String)} has been set prior to obtaining
   * and writing to the outputStream.
   *
   * @return The outputStream to write content to.
   */
  OutputStream outputStream();

  /**
   * Returns the path part of the request URI.
   *
   * @return The path part of the request URI.
   */
  String path();

  /**
   * Return the path parameter.
   *
   * @param name The path parameter name.
   */
  String pathParam(String name);

  /** Return all the path parameters as a map. */
  Map<String, String> pathParamMap();

  /**
   * Returns the port number used in the request.
   *
   * @return The port number of the request.
   */
  int port();

  /**
   * Returns the protocol used in the request (e.g., HTTP/1.1).
   *
   * @return The protocol of the request.
   */
  String protocol();

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
   * Return all the query parameters as a map.
   *
   * <p>Note this returns the first value for any given key if that key has multiple values.
   */
  Map<String, String> queryParamMap();

  /** Return all the query parameters for the given parameter name. */
  List<String> queryParams(String name);

  /** Return the request query string, or null. */
  String queryString();

  /**
   * Redirects the client to the specified location using a 302 (Found) status code.
   *
   * @param location The URL to redirect to.
   */
  void redirect(String location);

  /**
   * Redirects the client to the specified location using the given HTTP status code.
   *
   * @param location The URL to redirect to.
   * @param httpStatusCode The HTTP status code to use for the redirect.
   */
  void redirect(String location, int httpStatusCode);

  /**
   * Removes a cookie with the specified name.
   *
   * @param name The name of the cookie to remove.
   */
  Context removeCookie(String name);

  /**
   * Removes a cookie with the specified name and path.
   *
   * @param name The name of the cookie to remove.
   * @param path The path of the cookie to remove.
   */
  Context removeCookie(String name, String path);

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
   * @param name The template name
   * @param model The model used with the template
   */
  Context render(String name, Map<String, Object> model);

  /**
   * Returns the value of the specified response header.
   *
   * @param key The name of the header.
   * @return The value of the header, or null if not found.
   */
  String responseHeader(String key);

  /** Return true if the response has been sent. */
  boolean responseSent();

  /**
   * Returns a set of roles associated with the current route.
   *
   * @return A set of roles.
   */
  Set<Role> routeRoles();

  /** Return the request scheme. */
  String scheme();

  /**
   * Get the {@link SSLSession} for this exchange.
   *
   * @return the {@code SSLSession}
   */
  SSLSession sslSession();

  /** Return the current response status. */
  int status();

  /** Set the status code on the response. */
  default Context status(HttpStatus statusCode) {
    return status(statusCode.status());
  }

  /** Set the status code on the response. */
  Context status(int statusCode);

  /** Write plain text content to the response. */
  void text(String content);

  /** Return the request uri. */
  URI uri();

  /** Return the request user agent, or null. */
  default String userAgent() {
    return header(Constants.USER_AGENT);
  }

  /**
   * Writes the given bytes directly to the response.
   *
   * @param bytes The byte array to write.
   */
  void write(byte[] bytes);

  /**
   * Writes the content from the given InputStream directly to the response body.
   *
   * @param is The input stream containing the content to write.
   */
  void write(InputStream is);

  /**
   * Writes the given string content directly to the response.
   *
   * @param content The string content to write.
   */
  void write(String content);

  /**
   * This interface represents a cookie used in HTTP communication. Cookies are small pieces of data
   * sent from a server to a web browser and stored on the user's computer. They can be used to
   * store information about a user's session, preferences, or other data.
   */
  interface Cookie {

    /**
     * Cookie SameSite options.
     */
    enum SameSite {
     Strict, Lax, None
    }

    /**
     * Creates and returns a new expired cookie with the given name. This cookie will be sent to the
     * browser but will be immediately discarded. It's useful for removing existing cookies.
     *
     * @param name The name of the cookie.
     * @return A new expired cookie with the given name.
     */
    static Cookie expired(String name) {
      return DCookie.expired(name);
    }

    /**
     * Creates and returns a new cookie with the given name and value.
     *
     * @param name The name of the cookie.
     * @param value The value to store in the cookie.
     * @return A new cookie with the given name and value.
     */
    static Cookie of(String name, String value) {
      return DCookie.of(name, value);
    }

    /**
     * Returns the domain for which this cookie is valid.
     *
     * @return The domain associated with the cookie, or null if not set.
     */
    String domain();

    /**
     * Sets the domain for which this cookie is valid.
     *
     * @param domain The domain for which the cookie should be valid.
     * @return A new cookie instance with the updated domain.
     */
    Cookie domain(String domain);

    /**
     * Returns the date and time when this cookie expires.
     *
     * @return The expiration date and time of the cookie, or null if not set.
     */
    ZonedDateTime expires();

    /**
     * Sets the date and time when this cookie expires.
     *
     * @param expires The date and time when the cookie should expire.
     * @return A new cookie instance with the updated expires value.
     */
    Cookie expires(ZonedDateTime expires);

    /**
     * Indicates if the HttpOnly attribute is enabled for this cookie.
     *
     * <p>The HttpOnly attribute ensures that the cookie is inaccessible to JavaScript, helping to
     * mitigate cross-site scripting (XSS) attacks.
     *
     * @return {@code true} if the cookie has the HttpOnly attribute enabled, {@code false}
     *     otherwise
     */
    boolean httpOnly();

    /**
     * Sets the HttpOnly attribute for this cookie.
     *
     * <p>When enabled, the cookie will not be accessible via client-side scripts, providing
     * additional security against XSS attacks.
     *
     * @param httpOnly {@code true} to enable the HttpOnly attribute, {@code false} to disable it
     * @return this cookie instance with the updated HttpOnly attribute
     */
    Cookie httpOnly(boolean httpOnly);

    /**
     * Returns the maximum age (in seconds) of this cookie. An expired cookie (maxAge of 0) will be
     * deleted immediately by the browser.
     *
     * @return The maximum age of the cookie in seconds, or null if not set.
     */
    Duration maxAge();

    /**
     * Sets the maximum age (in seconds) of this cookie. An expired cookie (maxAge of 0) will be
     * deleted immediately by the browser.
     *
     * @param maxAge The maximum age of the cookie in seconds.
     * @return A new cookie instance with the updated maxAge.
     */
    Cookie maxAge(Duration maxAge);

    /**
     * Returns the name of this cookie.
     *
     * @return The name of the cookie.
     */
    String name();

    /**
     * Indicates if the Partitioned attribute is enabled for this cookie.
     */
    boolean partitioned();

    /**
     * Set the Partitioned attribute for this cookie.
     */
    Cookie partitioned(boolean partitioned);

    /**
     * Returns the path on the server for which this cookie is valid. Cookies are only sent to the
     * browser if the URL path starts with this value.
     *
     * @return The path associated with the cookie, or null if not set.
     */
    String path();

    /**
     * Sets the path on the server for which this cookie is valid. Cookies are only sent to the
     * browser if the URL path starts with this value.
     *
     * @param path The path on the server for which the cookie should be valid.
     * @return A new cookie instance with the updated path.
     */
    Cookie path(String path);

    /**
     * Return the SameSite setting.
     */
    SameSite sameSite();

    /**
     * Set the SameSite setting for this cookie.
     */
    Cookie sameSite(SameSite sameSite);

    /**
     * Indicates whether this cookie should only be sent over secure connections (HTTPS).
     *
     * @return True if the cookie should only be sent over secure connections, false otherwise.
     */
    boolean secure();

    /**
     * Sets the secure attribute for this cookie.
     *
     * <p>When enabled, the cookie will only be sent over secure HTTPS connections.
     *
     * @param secure {@code true} to enable the secure attribute, {@code false} to disable it
     * @return this cookie instance with the updated secure attribute
     */
    Cookie secure(boolean secure);

    /**
     * Returns content of the cookie as a 'Set-Cookie:' header value specified by <a
     * href="https://tools.ietf.org/html/rfc6265">RFC6265</a>.
     */
    @Override
    String toString();

    /**
     * Returns the value stored in this cookie.
     *
     * @return The value of the cookie.
     */
    String value();
  }
}
