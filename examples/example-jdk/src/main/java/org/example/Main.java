package org.example;

import io.avaje.jex.Jex;
import io.avaje.jex.http.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {

    Jex.create()
      //.attribute(Executor.class, Executors.newVirtualThreadExecutor())
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("hello"))
        .get("/foo/{id}", Main::fooBean)
        .get("/delay", Main::delay)
        .get("/dump", ctx -> dumpThreadCount())
      )
      .port(7004)
      .start();
  }

  private static void fooBean(Context ctx) {
    HelloDto bean = new HelloDto();
    bean.id = Integer.parseInt(ctx.pathParam("id"));
    bean.name = "Rob";
    ctx.json(bean);
  }

  private static void delay(Context ctx) {
    log.info("delay start");
    try {
      Thread.sleep(5_000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      e.printStackTrace();
    }
    ctx.text("delay done");
    log.info("delay done");
  }

  private static void dumpThreadCount() {
    Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
    System.out.println("Thread count: " + allStackTraces.size());
    Set<Thread> threads = allStackTraces.keySet();
    System.out.println("Threads: " + threads);
  }
}
