package io.avaje.jex;

@FunctionalInterface
public interface Handler {

  void handle(Context ctx);
}
