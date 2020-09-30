package io.avaje.jex.http;

public class BadGatewayResponse extends HttpResponseException {

  public BadGatewayResponse(String message) {
    super(502, message);
  }

  public BadGatewayResponse() {
    super(502, "Bad gateway");
  }

}
