package io.avaje.jex.http;

public class ServiceUnavailableResponse extends HttpResponseException {

  public ServiceUnavailableResponse(String message) {
    super(503, message);
  }

  public ServiceUnavailableResponse() {
    super(503, "Service unavailable");
  }

}
