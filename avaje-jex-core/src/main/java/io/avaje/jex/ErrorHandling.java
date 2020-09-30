package io.avaje.jex;

public interface ErrorHandling {

  /**
   * Register an exception handler for the given exception type.
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
   * Return a registered exception handler given the exception type or null
   * if one is not found.
   * <p>
   * This includes searching the super types of the exception.
   * </p>
   */
  <T extends Exception> ExceptionHandler<Exception> find(Class<T> exceptionType);

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
