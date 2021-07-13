package org.example;

import io.avaje.jex.Jex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

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
        .get("/delay", ctx -> {
          log.info("delay start");
          try {
            Thread.sleep(5_000);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
          }
          ctx.text("delay done");
          log.info("delay done");
        })
      )
      .staticFiles().addClasspath("/static", "content")
//      .staticFiles().addExternal("/", "/tmp/junk")
      .port(7003)
      .start();
  }
}
