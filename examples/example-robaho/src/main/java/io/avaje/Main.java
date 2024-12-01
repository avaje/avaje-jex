package io.avaje;

import io.avaje.jex.Jex;

public class Main {

  public static void main(String[] args) {

    // Below system property is NOT required as it will register via service loading
    // System.setProperty("com.sun.net.httpserver.HttpServerProvider", "robaho.net.httpserver.DefaultHttpServerProvider");

    Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("root"))
        .get("/one", ctx -> ctx.text("one"))
        .get("/two/{name}", ctx -> {
          ctx.text("two Yo " + ctx.pathParam("name"));
        })
        .post("one", ctx -> ctx.text("posted")))
      .port(7002)
      .start();

  }
}
