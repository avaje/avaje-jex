package io.avaje.jex.http;

public class UnauthorizedResponse extends HttpResponseException {

  public UnauthorizedResponse(String message) {
    super(401, message);
  }

  public UnauthorizedResponse() {
    super(401, "Unauthorized");
  }

}
