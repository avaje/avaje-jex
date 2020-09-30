package io.avaje.jex;

import java.util.HashMap;
import java.util.Map;

class DefaultErrorHandling implements ErrorHandling {

  private final Map<Class<?>, ExceptionHandler<?>> handlers = new HashMap<>();

  @Override
  public <T extends Exception> ErrorHandling exception(Class<T> type, ExceptionHandler<T> handler) {
    handlers.put(type, handler);
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

  @SuppressWarnings("unchecked")
  public <T extends Exception> ExceptionHandler<Exception> find(Class<T> exceptionType) {
    Class<?> type = exceptionType;
    do {
      final ExceptionHandler<?> handler = handlers.get(type);
      if (handler != null) {
        return (ExceptionHandler<Exception>) handler;
      }
      type = type.getSuperclass();
    } while (type != null);
    return null;
  }

}
