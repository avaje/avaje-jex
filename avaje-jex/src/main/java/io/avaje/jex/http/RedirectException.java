package io.avaje.jex.http;

/** Thrown when redirecting */
public class RedirectException extends HttpResponseException {

  public RedirectException(String message) {
    super(302, message);
  }
}
