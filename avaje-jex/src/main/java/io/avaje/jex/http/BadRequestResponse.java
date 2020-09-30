package io.avaje.jex.http;

public class BadRequestResponse extends HttpResponseException {

  public BadRequestResponse(String message) {
    super(400, message);
  }

  public BadRequestResponse() {
    super(400, "Bad request");
  }

}
