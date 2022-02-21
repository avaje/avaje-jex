package org.example;

import io.avaje.inject.BeanScope;
import io.avaje.jex.Context;
import io.avaje.jex.Jex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMain {

  private static final Logger log = LoggerFactory.getLogger(JMain.class);

  public static void main(String[] args) {
    new JMain().start(BeanScope.newBuilder().build());
  }

  void start(BeanScope beanScope) {

    Jex.create()
      .configureWith(beanScope)
      .routing(routing -> routing
        .get("/foo/{id}", JMain::helloBean)
        .get("/hi", JMain::hello)
        .get("/delay", JMain::delay)
      )
      .staticFiles().addClasspath("/static", "content")
//      .staticFiles().addExternal("/", "/tmp/junk")
      .port(7003)
      .start();
  }

  private static void hello(Context context) {
    context.text("hello");
  }

  private static void helloBean(Context ctx) {
    HelloDto bean = new HelloDto();
    bean.id = Integer.parseInt(ctx.pathParam("id"));
    bean.name = "Rob";
    ctx.json(bean);
  }

  private static void delay(Context ctx) {
    log.info("delay start");
    try {
      Thread.sleep(15_000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      e.printStackTrace();
    }
    ctx.text("delay done");
    log.info("delay done");
  }

}
