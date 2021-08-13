package io.avaje.jex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

class DefaultLifecycle implements AppLifecycle {

  private static final Logger log = LoggerFactory.getLogger(Jex.class);

  private final List<Pair> shutdownRunnable = new ArrayList<>();

  private final ReentrantLock lock = new ReentrantLock();

  private Status status = Status.STARTING;

  @Override
  public void onShutdown(Runnable onShutdown) {
    onShutdown(onShutdown, 1000);
  }

  @Override
  public void onShutdown(Runnable onShutdown, int order) {
    lock.lock();
    try {
      shutdownRunnable.add(new Pair(onShutdown, order));
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void registerShutdownHook(Runnable onShutdown) {
    Hook hook = new Hook(onShutdown);
    Runtime.getRuntime().addShutdownHook(hook);
  }

  static class Hook extends Thread {
    Hook(Runnable runnable) {
      super(runnable, "JexHook");
    }

    @Override
    public void run() {
      super.run();
    }
  }

  @Override
  public Status status() {
    return status;
  }

  @Override
  public void status(Status newStatus) {
    lock.lock();
    try {
      if (newStatus == Status.STOPPED) {
        fireOnShutdown();
      }
      status = newStatus;
    } finally {
      lock.unlock();
    }
  }

  private void fireOnShutdown() {
    Collections.sort(shutdownRunnable);
    for (Pair pair : shutdownRunnable) {
      try {
        pair.callback.run();
      } catch (Exception e) {
        log.error("Error running shutdown runnable", e);
        // maybe logging has stopped so also do ...
        e.printStackTrace();
      }
    }
    log.info("Jex shutdown complete");
  }

  static class Pair implements Comparable<Pair> {
    private final Runnable callback;
    private final int order;

    Pair(Runnable callback, int order) {
      this.callback = callback;
      this.order = order;
    }

    @Override
    public int compareTo(Pair other) {
      return Integer.compare(order, other.order);
    }
  }

}
