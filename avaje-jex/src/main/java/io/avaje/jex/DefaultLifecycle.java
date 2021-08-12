package io.avaje.jex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

class DefaultLifecycle implements AppLifecycle {

  private static final Logger log = LoggerFactory.getLogger(AppLifecycle.class);

  private final List<Runnable> shutdownRunnable = new ArrayList<>();

  private final ReentrantLock lock = new ReentrantLock();

  private Status status = Status.STARTING;

  @Override
  public void onShutdown(Runnable onShutdown) {
    lock.lock();
    try {
      shutdownRunnable.add(onShutdown);
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
      if (status != newStatus) {
        status = newStatus;
      }
    } finally {
      lock.unlock();
    }
  }

  private void fireOnShutdown() {
    for (Runnable shutdownRunnable : shutdownRunnable) {
      try {
        shutdownRunnable.run();
      } catch (Exception e) {
        log.error("Error running shutdown runnable", e);
        // maybe logging has stopped so also do ...
        e.printStackTrace();
      }
    }
  }

}
