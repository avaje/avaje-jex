package io.avaje.jex.spi;

import io.avaje.jex.Context;
import io.avaje.jex.Routing;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Extension to Context for processing the request.
 */
public interface SpiContext extends Context {

  String TEXT_HTML = "text/html";
  String TEXT_PLAIN = "text/plain";
  String TEXT_HTML_UTF8 = "text/html;charset=utf-8";
  String TEXT_PLAIN_UTF8 = "text/plain;charset=utf-8";
  String APPLICATION_JSON = "application/json";
  String APPLICATION_X_JSON_STREAM = "application/x-json-stream";

  /**
   * Return the response outputStream to write content to.
   */
  OutputStream outputStream();

  /**
   * Return the request inputStream to read content from.
   */
  InputStream inputStream();

  /**
   * Set to indicate BEFORE, ExchangeHandler and AFTER modes of the request.
   */
  void setMode(Routing.Type type);

  /**
   * Perform the redirect as part of Exception handling. Typically due to before handler.
   */
  void performRedirect();
}
