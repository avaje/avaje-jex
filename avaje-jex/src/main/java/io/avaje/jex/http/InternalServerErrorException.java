package io.avaje.jex.http;

/** Thrown when server has an internal error */
public class InternalServerErrorException extends HttpResponseException {

  public InternalServerErrorException(String message) {
    super(500, message);
  }

  public InternalServerErrorException(Object jsonResponse) {
    super(500, jsonResponse);
  }
}
