package io.avaje.jex.http;

/** Thrown when server has an internal error */
public class InternalServerErrorException extends HttpResponseException {

  /** Create with a message. */
  public InternalServerErrorException(String message) {
    super(500, message);
  }

  /** Create with a status and response that will sent as JSON. */
  public InternalServerErrorException(Object jsonResponse) {
    super(500, jsonResponse);
  }
}
