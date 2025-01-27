package io.avaje;

import io.avaje.jex.Jex;

public class Main {

  public static void main(String[] args) {

    Jex.create()
        .get("/", ctx -> ctx.text("" + Thread.currentThread().isVirtual()))
        .get("/one", ctx -> {
          System.out.println("one");

          ctx.text("one");})
        .get(
            "/two/{name}",
            ctx -> {
              ctx.text("two Yo " + ctx.pathParam("name"));
            })
        .post("one", ctx -> ctx.text("posted"))
        .port(7002)
        .start();
  }
}
