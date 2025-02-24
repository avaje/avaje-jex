package io.avaje.jex;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/** Defines the lifecycle configuration for an application. */
public final class AppLifecycle {

  /** Represents the possible states of the application server. */
  public enum Status {
    STARTING,
    STARTED,
    STOPPING,
    STOPPED
  }

  private static final Logger log = System.getLogger("io.avaje.jex");

  private final List<Pair> shutdownRunnable = new ArrayList<>();
  private final ReentrantLock lock = new ReentrantLock();
  private final AtomicInteger next = new AtomicInteger(1000);
  private final AtomicBoolean jvmStop = new AtomicBoolean();
  private Status status = Status.STARTING;
  private Hook shutdownHook;

  AppLifecycle() {}

  /**
   * Registers a runnable to be executed when the application server is shutting down.
   *
   * <p>This runnable will be executed after all active requests have been processed.
   *
   * @param onShutdown The runnable to execute on shutdown.
   */
  public void onShutdown(Runnable onShutdown) {
    onShutdown(onShutdown, next.getAndIncrement());
  }

  /**
   * Registers a runnable to be executed when the application server is shutting down, with a
   * specific order.
   *
   * <p>Runnables with lower order values will be executed first.
   *
   * <p>This runnable will be executed after all active requests have been processed.
   *
   * @param onShutdown The runnable to execute on shutdown.
   * @param order The order in which to execute the runnable.
   */
  public void onShutdown(Runnable onShutdown, int order) {
    lock.lock();
    try {
      shutdownRunnable.add(new Pair(onShutdown, order));
    } finally {
      lock.unlock();
    }
  }

  /**
   * Registers a runnable as a shutdown hook with the JVM.
   *
   * <p>This runnable will be executed when the JVM is shutting down, regardless of the application
   * server's state.
   *
   * @param onShutdown The runnable to register as a shutdown hook.
   */
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

  /**
   * Returns the current status of the application server.
   *
   * @return The current status of the server.
   */
  public Status status() {
    return status;
  }

  /**
   * Sets the current status of the application server.
   *
   * @param newStatus The new status to set.
   */
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

  /**
   * Indicates whether the application server is currently starting or has started.
   *
   * @return true if the server is starting or started, false otherwise.
   */
  public boolean isAlive() {
    return status == Status.STARTING || status == Status.STARTED;
  }

  /**
   * Indicates whether the application server has fully started.
   *
   * @return true if the server has started, false otherwise.
   */
  public boolean isReady() {
    return status == Status.STARTED;
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

  private static final class Hook extends Thread {
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

  private static final class Pair implements Comparable<Pair> {
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
