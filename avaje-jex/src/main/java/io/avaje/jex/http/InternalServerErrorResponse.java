package io.avaje.jex.http;

public class InternalServerErrorResponse extends HttpResponseException {

  public InternalServerErrorResponse(String message) {
    super(500, message);
  }

  public InternalServerErrorResponse() {
    super(500, "Internal server error");
  }

}
