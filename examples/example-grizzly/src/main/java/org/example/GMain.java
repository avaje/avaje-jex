package org.example;

import io.avaje.jex.Jex;
import io.avaje.jex.core.HealthPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GMain {

  private static final Logger log = LoggerFactory.getLogger(GMain.class);

  public static void main(String[] args) throws InterruptedException {

    Jex.create()
      .attribute(Executor.class, Executors.newVirtualThreadExecutor())
      .routing(routing -> routing
        //.get("/", ctx -> ctx.text("hello world"))
        .get("/", ctx -> ctx.json(HelloDto.rob())) //.header("x2-foo","asd")
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
        .get("/dump", ctx -> dumpThreadCount())
      )
      .configure(new HealthPlugin())
      .port(7003)
      .start();

    Thread.currentThread().join();
  }

  private static void dumpThreadCount() {
    Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
    System.out.println("Thread count: " + allStackTraces.size());
    Set<Thread> threads = allStackTraces.keySet();
    System.out.println("Threads: " + threads);
  }
}
