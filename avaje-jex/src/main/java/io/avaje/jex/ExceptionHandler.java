package io.avaje.jex;

public interface ExceptionHandler<T extends Exception>  {

  void handle(T exception, Context ctx);

}
