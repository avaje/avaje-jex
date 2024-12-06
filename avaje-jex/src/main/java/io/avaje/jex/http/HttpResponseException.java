package io.avaje.jex.http;

import java.util.Collections;
import java.util.Map;

/**
 * Throwing an uncaught {@code HttpResponseException} will interrupt http processing and set the
 * status code and response body with the given message
 */
public class HttpResponseException extends RuntimeException {

  private final int status;

  public HttpResponseException(int status, String message) {
    super(message);
    this.status = status;
  }

  public int getStatus() {
    return status;
  }
}
