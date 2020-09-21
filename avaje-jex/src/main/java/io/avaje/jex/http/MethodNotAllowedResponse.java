package io.avaje.jex.http;

import java.util.Map;

public class MethodNotAllowedResponse extends HttpResponseException {

  public MethodNotAllowedResponse(String message, Map<String, String> details) {
    super(403, message, details);
  }

  public MethodNotAllowedResponse(String message) {
    super(403, message);
  }

}
