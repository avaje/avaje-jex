package io.avaje.jex.http;

/**
 * Throwing an uncaught {@code HttpResponseException} will interrupt http processing and set the
 * status code and response body with the given message or json body
 */
public class HttpResponseException extends RuntimeException {

  private final int status;
  private final Object jsonResponse;

  /**
   * @param status the http status to send
   * @param message the exception message that will be sent back in the response
   */
  public HttpResponseException(int status, String message) {
    super(message);
    this.status = status;
    this.jsonResponse = null;
  }

  /**
   * @param status the http status to send
   * @param jsonResponse the response body that will be sent back as json
   */
  public HttpResponseException(int status, Object jsonResponse) {

    this.status = status;
    this.jsonResponse = jsonResponse;
  }

  public int getStatus() {
    return status;
  }

  public Object jsonResponse() {
    return jsonResponse;
  }
}
