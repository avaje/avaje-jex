package io.avaje.jex.http;

/** Thrown when request is invalid */
public class BadRequestException extends HttpResponseException {

  public BadRequestException(String message) {
    super(400, message);
  }
}
