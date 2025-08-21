package io.avaje.jex.ssl;

/**
 * SSL Configuration exception.
 */
public class SslConfigException extends RuntimeException {

  public SslConfigException(String message, Throwable t) {
    super(message, t);
  }

  public SslConfigException(String message) {
    super(message);
  }
}
