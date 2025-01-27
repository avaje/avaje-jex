package org.example;

import java.util.Map;
import java.util.Set;

import io.avaje.jex.Context;
import io.avaje.jex.Jex;
import io.avaje.jex.core.json.JsonbJsonService;
import io.avaje.jsonb.Jsonb;

public class Main {

  private static final  System.Logger log = System.getLogger("org.example");

  public static void main(String[] args) {

    Jsonb jsonb = Jsonb.builder().build();

    Jex.create()
      .config(config -> config.jsonService(new JsonbJsonService(jsonb)))
      //.attribute(Executor.class, Executors.newVirtualThreadPerTaskExecutor())
      .routing(routing -> routing
        .get("/", ctx -> ctx.text("hello world"))
        .get("/kevin", Main::version)
        .get("/foo/{id}", Main::fooBean)
        .get("/delay", Main::delay)
        .get("/dump", ctx -> {
          dumpThreadCount();
          ctx.text("done");
        })
      )
      .port(7003)
      .start();
  }

  private static void version(Context ctx) {
    ctx.text("version 1.4");
    log.log(System.Logger.Level.INFO, "hello version 1.4");
  }

  private static void fooBean(Context ctx) {
    HelloDto bean = new HelloDto();
    bean.id = Integer.parseInt(ctx.pathParam("id"));
    bean.name = "Rob";
    ctx.json(bean);
  }

  private static void delay(Context ctx) {
    log.log(System.Logger.Level.INFO, "delay start");
    try {
      Thread.sleep(5_000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      e.printStackTrace();
    }
    ctx.text("delay done");
    log.log(System.Logger.Level.INFO, "delay done");
  }

  private static void dumpThreadCount() {
    Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
    System.out.println("Thread count: " + allStackTraces.size());
    Set<Thread> threads = allStackTraces.keySet();
    System.out.println("Threads: " + threads);
  }
}
