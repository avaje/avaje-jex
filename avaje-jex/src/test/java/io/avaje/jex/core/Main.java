package io.avaje.jex.core;

import io.avaje.jex.Jex;

public class Main {

  public static void main(String[] args) {

    Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("hello world"))
      )
      .port(9009)
      .start();
  }
}
