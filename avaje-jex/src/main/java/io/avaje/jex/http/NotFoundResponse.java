package io.avaje.jex.http;

public class NotFoundResponse extends HttpResponseException {

  public NotFoundResponse(String message) {
    super(404, message);
  }

  public NotFoundResponse() {
    super(404, "Not found");
  }

}
