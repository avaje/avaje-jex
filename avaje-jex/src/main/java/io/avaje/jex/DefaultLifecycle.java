package io.avaje.jex;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

final class DefaultLifecycle implements AppLifecycle {

  private static final System.Logger log = System.getLogger("io.avaje.jex");

  private final List<Pair> shutdownRunnable = new ArrayList<>();
  private final ReentrantLock lock = new ReentrantLock();
  private final AtomicInteger next = new AtomicInteger(1000);
  private final AtomicBoolean jvmStop = new AtomicBoolean();
  private Status status = Status.STARTING;
  private Hook shutdownHook;

  @Override
  public void onShutdown(Runnable onShutdown) {
    onShutdown(onShutdown, next.getAndIncrement());
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
    lock.lock();
    try {
      if (shutdownHook == null) {
        shutdownHook = new Hook(onShutdown, jvmStop);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
      }
    } finally {
      lock.unlock();
    }
  }

  static final class Hook extends Thread {
    private final AtomicBoolean jvmStop;

    Hook(Runnable runnable, AtomicBoolean jvmStop) {
      super(runnable, "JexHook");
      this.jvmStop = jvmStop;
    }

    @Override
    public void run() {
      jvmStop.set(true);
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
        log.log(Level.ERROR, "Error running shutdown runnable", e);
        // maybe logging has stopped so also do ...
        e.printStackTrace();
      }
    }
    if (!jvmStop.get()) {
      removeShutdownHook();
    }
    log.log(Level.INFO, "Jex shutdown complete");
  }

  private void removeShutdownHook() {
    if (shutdownHook != null) {
      Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }
  }

  static final class Pair implements Comparable<Pair> {
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
