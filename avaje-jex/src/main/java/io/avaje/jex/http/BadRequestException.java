package io.avaje.jex.http;

/** Thrown when request is invalid */
public class BadRequestException extends HttpResponseException {

  /** Create with a message. */
  public BadRequestException(String message) {
    super(400, message);
  }

  /** Create with a response that will sent as JSON. */
  public BadRequestException(Object jsonResponse) {
    super(400, jsonResponse);
  }
}
