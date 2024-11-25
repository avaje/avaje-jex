package io.avaje.jex.http;

/** Thrown when unable to find a route/resource */
public class BadRequestException extends HttpResponseException {

  public BadRequestException(String message) {
    super(404, message);
  }
}
