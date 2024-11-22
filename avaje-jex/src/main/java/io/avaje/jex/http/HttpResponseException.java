package io.avaje.jex.http;

import java.util.Collections;
import java.util.Map;

public class HttpResponseException extends RuntimeException {

  private final int status;
  private final Map<String, String> details;

  public HttpResponseException(int status, String message, Map<String, String> details) {
    super(message);
    this.status = status;
    this.details = details;
  }

  public HttpResponseException(int status, String message) {
    this(status, message, Collections.emptyMap());
  }

  public HttpResponseException(ErrorCode code) {
    this(code.status(), code.message());
  }

  public HttpResponseException(ErrorCode code, Map<String, String> details) {
    this(code.status(), code.message(), details);
  }

  public int getStatus() {
    return status;
  }

  public Map<String, String> getDetails() {
    return details;
  }
}
