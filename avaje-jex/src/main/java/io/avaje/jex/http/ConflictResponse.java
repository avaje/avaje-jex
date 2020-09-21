package io.avaje.jex.http;

public class ConflictResponse extends HttpResponseException {

  public ConflictResponse(String message) {
    super(409, message);
  }

  public ConflictResponse() {
    super(409, "Conflict");
  }

}
