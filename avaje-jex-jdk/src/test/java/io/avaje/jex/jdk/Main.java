package io.avaje.jex.jdk;

import io.avaje.jex.Jex;
import io.avaje.jex.core.HealthPlugin;

public class Main {

  public static void main(String[] args) {

    Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("hello world"))
      )
      .configure(new HealthPlugin())
      .port(9009)
      .start();
  }
}
