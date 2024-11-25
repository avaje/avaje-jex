package io.avaje.jex.http;

/** Thrown when unable to find a route/resource */
public class NotFoundException extends HttpResponseException {

  public NotFoundException(String message) {
    super(404, message);
  }
}
