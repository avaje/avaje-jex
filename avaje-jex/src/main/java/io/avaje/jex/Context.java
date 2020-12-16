package io.avaje.jex;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

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
   * Attributes are available to other handlers in the request lifecycle
   */
  Context attribute(String key, Object value);

  /**
   * Get the specified attribute from the request.
   */
  <T> T attribute(String key);

  /**
   * Gets a map with all the attribute keys and values on the request.
   */
  Map<String, Object> attributeMap();

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
   * Return the splat path value for the given position.
   *
   * @param position the index postion of the splat starting with 0.
   */
  String splat(int position);

  /**
   * Return all the splat values.
   */
  List<String> splats();

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
  String formParam(String key);

  /**
   * Return the first form param value for the specified key or the default value.
   */
  String formParam(String key, String defaultValue);

  /**
   * Return the form params for the specified key, or empty list.
   */
  List<String> formParams(String key);

  /**
   * Returns a map with all the form param keys and values.
   */
  Map<String, List<String>> formParamMap();

  /**
   * Return the request scheme.
   */
  String scheme();

  /**
   * Sets an attribute for the user session.
   */
  Context sessionAttribute(String key, Object value);

  /**
   * Gets specified attribute from the user session, or null.
   */
  <T> T sessionAttribute(String key);

  /**
   * Return a map of all the attributes in the user session.
   */
  Map<String, Object> sessionAttributeMap();

  /**
   * Return the request url.
   */
  String url();

  /**
   * Return the full request url, including query string (if present)
   */
  String fullUrl();

  /**
   * Return the request context path.
   */
  String contextPath();

  /**
   * Return the request user agent, or null.
   */
  String userAgent();

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
   * Write raw content to the response.
   */
  Context write(String content);

  /**
   * Render a template typically as html.
   *
   * @param name The template name
   */
  Context render(String name);

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
  void header(String key, String value);

  /**
   * Returns the request host, or null.
   */
  String host();

  /**
   * Returns the request IP.
   */
  String ip();

  /**
   * Returns true if request is multipart.
   */
  boolean isMultipart();

  /**
   * Returns true if request is multipart/form-data.
   */
  boolean isMultipartFormData();

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
   * Return the underlying http servlet request.
   */
  HttpServletRequest req();

  /**
   * Return the underlying http servlet response.
   */
  HttpServletResponse res();

  /**
   * Return the first UploadedFile for the specified name or null.
   */
  UploadedFile uploadedFile(String name);

  /**
   * Return a list of UploadedFiles for the specified name, or empty list.
   */
  List<UploadedFile> uploadedFiles(String name);
}
