package io.avaje.jex.http;

public class GoneResponse extends HttpResponseException {

  public GoneResponse(String message) {
    super(410, message);
  }

  public GoneResponse() {
    super(410, "Gone");
  }

}
