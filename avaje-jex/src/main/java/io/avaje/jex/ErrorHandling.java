package io.avaje.jex;

public interface ErrorHandling {

  /**
   * Adds an exception mapper to the instance.
   */
  <T extends Exception> ErrorHandling exception(Class<T> exceptionClass, ExceptionHandler<T> handler);

  /**
   * Adds an error mapper to the instance.
   * Useful for turning error-codes (404, 500) into standardized messages/pages
   */
  ErrorHandling error(int statusCode, Handler handler);

  /**
   * Adds an error mapper for the specified content-type to the instance.
   * Useful for turning error-codes (404, 500) into standardized messages/pages
   */
  ErrorHandling error(int statusCode, String contentType, Handler handler);

  /**
   * Adds to the Routing.
   */
  @FunctionalInterface
  interface Service {

    /**
     * Add to the error handling.
     */
    void add(ErrorHandling errorHandling);
  }
}
