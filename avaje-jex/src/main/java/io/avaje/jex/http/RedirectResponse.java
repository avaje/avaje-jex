package io.avaje.jex.http;

public class RedirectResponse extends HttpResponseException {

  public RedirectResponse(String message) {
    super(302, message);
  }

  public RedirectResponse() {
    super(302, "Redirect");
  }

}
