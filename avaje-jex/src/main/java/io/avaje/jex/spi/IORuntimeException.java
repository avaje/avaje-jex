package io.avaje.jex.spi;

/**
 * Wraps IOException as a RuntimeException when not expected to be handled.
 */
public class IORuntimeException extends RuntimeException {

  /**
   * Create with a checked exception.
   */
  public IORuntimeException(Exception cause) {
    super(cause);
  }
}
