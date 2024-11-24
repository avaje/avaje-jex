package io.avaje.jex;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

  final class Cookie {
    private static final ZonedDateTime EXPIRED = ZonedDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0, 0), ZoneId.of("GMT"));
    private static final DateTimeFormatter RFC_1123_DATE_TIME = DateTimeFormatter.RFC_1123_DATE_TIME;
    private static final String PARAM_SEPARATOR = "; ";
    private final String name; // NAME= ... "$Name" style is reserved
    private final String value; // value of NAME
    private String domain; // ;Domain=VALUE ... domain that sees cookie
    private ZonedDateTime expires;
    private Duration maxAge;// = -1; // ;Max-Age=VALUE ... cookies auto-expire
    private String path; // ;Path=VALUE ... URLs that see the cookie
    private boolean secure; // ;Secure ... e.g. use SSL
    private boolean httpOnly;

    private Cookie(String name, String value) {
      if (name == null || name.isEmpty()) {
        throw new IllegalArgumentException("name required");
      }
      this.name = name;
      this.value = value;
    }

    public static Cookie expired(String name) {
      return new Cookie(name, "").expires(EXPIRED);
    }

    public static Cookie of(String name, String value) {
      return new Cookie(name, value);
    }

    public String name() {
      return name;
    }

    public String value() {
      return value;
    }

    public String domain() {
      return domain;
    }

    public Cookie domain(String domain) {
      this.domain = domain;
      return this;
    }

    public Duration maxAge() {
      return maxAge;
    }

    public Cookie maxAge(Duration maxAge) {
      this.maxAge = maxAge;
      return this;
    }

    public ZonedDateTime expires() {
      return expires;
    }

    public Cookie expires(ZonedDateTime expires) {
      this.expires = expires;
      return this;
    }

    public String path() {
      return path;
    }

    public Cookie path(String path) {
      this.path = path;
      return this;
    }

    public boolean secure() {
      return secure;
    }

    public Cookie secure(boolean secure) {
      this.secure = secure;
      return this;
    }

    public boolean httpOnly() {
      return httpOnly;
    }

    public Cookie httpOnly(boolean httpOnly) {
      this.httpOnly = httpOnly;
      return this;
    }

    /**
     * Returns content of this instance as a 'Set-Cookie:' header value specified
     * by <a href="https://tools.ietf.org/html/rfc6265">RFC6265</a>.
     */
    @Override
    public String toString() {
      StringBuilder result = new StringBuilder(60);
      result.append(name).append('=').append(value);
      if (expires != null) {
        result.append(PARAM_SEPARATOR);
        result.append("Expires=");
        result.append(expires.format(RFC_1123_DATE_TIME));
      }
      if ((maxAge != null) && !maxAge.isNegative() && !maxAge.isZero()) {
        result.append(PARAM_SEPARATOR);
        result.append("Max-Age=");
        result.append(maxAge.getSeconds());
      }
      if (domain != null) {
        result.append(PARAM_SEPARATOR);
        result.append("Domain=");
        result.append(domain);
      }
      if (path != null) {
        result.append(PARAM_SEPARATOR);
        result.append("Path=");
        result.append(path);
      }
      if (secure) {
        result.append(PARAM_SEPARATOR);
        result.append("Secure");
      }
      if (httpOnly) {
        result.append(PARAM_SEPARATOR);
        result.append("HttpOnly");
      }
      return result.toString();
    }
  }

}
