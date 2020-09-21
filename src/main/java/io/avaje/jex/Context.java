package io.avaje.jex;

import java.util.Map;

public interface Context {

  /**
   * Return the matched path as a raw expression.
   */
  String matchedPath();

  /***
   * Return the request body as bean.
   * @param clazz The bean type
   */
  <T> T bodyAsClass(Class<T> clazz);

  /**
   * Return the request body as bytes.
   */
  byte[] bodyAsBytes();

  /**
   * Return the request body as String.
   */
  String body();

  /**
   * Return all the path parameters as a map.
   */
  Map<String, String> pathParams();

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
   * Set the response text content.
   */
  Context text(String content);

  /**
   * Set the response body as JSON for the given bean.
   */
  Context json(Object bean);

  /**
   * Set the response content type.
   */
  Context contentType(String contentType);

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
}
