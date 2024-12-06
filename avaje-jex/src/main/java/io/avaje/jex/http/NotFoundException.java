package io.avaje.jex.http;

/** Thrown when unable to find a route/resource */
public class NotFoundException extends HttpResponseException {

  /** Create with a message. */
  public NotFoundException(String message) {
    super(404, message);
  }

  /** Create with a response that will sent as JSON. */
  public NotFoundException(Object jsonResponse) {
    super(404, jsonResponse);
  }
}
