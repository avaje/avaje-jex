package io.avaje.jex;

class DefaultErrorHandling implements ErrorHandling {

  @Override
  public <T extends Exception> ErrorHandling exception(Class<T> type, ExceptionHandler<T> handler) {

    return this;
  }

  @Override
  public ErrorHandling error(int statusCode, Handler handler) {
    return null;
  }

  @Override
  public ErrorHandling error(int statusCode, String contentType, Handler handler) {
    return null;
  }
}
