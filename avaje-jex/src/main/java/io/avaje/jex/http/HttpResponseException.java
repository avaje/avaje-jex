package io.avaje.jex.http;

/**
 * Throwing an uncaught {@code HttpResponseException} will interrupt http processing and set the
 * status code and response body with the given message or json body
 */
public class HttpResponseException extends RuntimeException {

  private final int status;
  private final Object jsonResponse;

  /**
   * Create with a status and use the default message for that status.
   *
   * @param status  the http status to send
   */
  public HttpResponseException(HttpStatus status) {
    super(status, status.message());
  }

  /**
   * Create with a status and message.
   *
   * @param status  the http status to send
   * @param message the exception message that will be sent back in the response
   */
  public HttpResponseException(HttpStatus status, String message) {
    super(status.status(), message);
  }
  
  /**
   * Create with a status and message.
   *
   * @param status  the http status to send
   * @param message the exception message that will be sent back in the response
   */
  public HttpResponseException(int status, String message) {
    super(message);
    this.status = status;
    this.jsonResponse = null;
  }

  /**
   * Create with a status and response that will sent as JSON.
   *
   * @param status       the http status to send
   * @param jsonResponse the response body that will be sent back as json
   */
  public HttpResponseException(int status, Object jsonResponse) {
    this.status = status;
    this.jsonResponse = jsonResponse;
  }

  /**  Return the status code. */
  public int status() {
    return status;
  }

  /** Return the response body that will sent as JSON. */
  public Object jsonResponse() {
    return jsonResponse;
  }
}
