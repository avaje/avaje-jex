package io.avaje.jex.http;

/** Thrown when unable to find a route/resource */
public class InternalServerErrorException extends HttpResponseException {

  public InternalServerErrorException(String message) {
    super(500, message);
  }
}
