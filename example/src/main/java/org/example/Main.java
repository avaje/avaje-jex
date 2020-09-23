package org.example;

import io.avaje.jex.Jex;
import io.avaje.jex.StaticFileSource;

import static io.avaje.jex.StaticFileSource.Location.CLASSPATH;

public class Main {

  public static void main(String[] args) {

    Jex.create()
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("hello world"))
        .get("/foo/{id}", ctx -> {
          HelloDto bean = new HelloDto();
          bean.id = Integer.parseInt(ctx.pathParam("id"));
          bean.name = "Rob";
          ctx.json(bean);
        })
      )
      .staticFiles().addClasspath("/static", "static-content")
      .port(7003)
      .start();
  }
}
