package io.avaje.jex.http;

public class RedirectResponse extends HttpResponseException {

  /**
   * Redirect with the given message.
   */
  public RedirectResponse(String message) {
    super(302, message);
  }

  /**
   * Redirect with the given http status code.
   */
  public RedirectResponse(int statusCode) {
    super(statusCode, null);
  }

  /**
   * Redirect with 302 http status code.
   */
  public RedirectResponse() {
    super(302, "Redirect");
  }

}
