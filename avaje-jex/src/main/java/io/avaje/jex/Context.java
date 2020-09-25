package io.avaje.jex;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
  Context attribute(String key, Object value);// = req.setAttribute(key, value)

  /**
   * Get the specified attribute from the request.
   */
  <T> T attribute(String key); // req.getAttribute(key) as? T

  /**
   * Gets a map with all the attribute keys and values on the request.
   */
  //<T>
  Map<String,Object> attributeMap();// = req.attributeNames.asSequence().associate { it to attribute<T>(it) }

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
   * Return all the query parameters as a map.
   * <p>
   * Note this returns the first value for any given key if that key has multiple values.
   */
  Map<String, String>  queryParamMap();

  /**
   * Set the status code on the response.
   */
  Context status(int statusCode);

  /**
   * Set the response content type.
   */
  Context contentType(String contentType);

  /**
   * Return the content type currently set on the response.
   */
  String contentType();

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
   * Return the underlying http servlet request.
   */
  HttpServletRequest req();

  /**
   * Return the underlying http servlet response.
   */
  HttpServletResponse res();
}
