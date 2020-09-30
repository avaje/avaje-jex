package io.avaje.jex.http;

public class GatewayTimeoutResponse extends HttpResponseException {

  public GatewayTimeoutResponse(String message) {
    super(504, message);
  }

  public GatewayTimeoutResponse() {
    super(504, "Gateway timeout");
  }

}
