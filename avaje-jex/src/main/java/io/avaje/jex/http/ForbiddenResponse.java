package io.avaje.jex.http;

public class ForbiddenResponse extends HttpResponseException {

  public ForbiddenResponse(String message) {
    super(403, message);
  }

  public ForbiddenResponse() {
    super(403, "Forbidden");
  }

}
