package io.avaje.jex;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

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
  //<T>
  Map<String, Object> attributeMap();

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
  Map<String,Object> sessionAttributeMap();

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
}
