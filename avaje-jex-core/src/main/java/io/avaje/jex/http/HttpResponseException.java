package io.avaje.jex.http;

import java.util.Collections;
import java.util.Map;

public class HttpResponseException extends RuntimeException {

  private int status;
  private Map<String, String> details;

  public HttpResponseException(int status, String message, Map<String, String> details) {
    super(message);
    this.status = status;
    this.details = details;
  }

  public HttpResponseException(int status, String message) {
    this(status, message, Collections.emptyMap());
  }

  public int getStatus() {
    return status;
  }

  public Map<String, String> getDetails() {
    return details;
  }
}
