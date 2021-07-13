package io.avaje.jex;

import java.util.concurrent.locks.ReentrantLock;

class DefaultLifecycle implements AppLifecycle {

  //private final List<Listener> listeners = new ArrayList<>();

  private final ReentrantLock lock = new ReentrantLock();

  private Status status = Status.STARTING;

  private Hook hook;

  @Override
  public void registerShutdownHook(Runnable onShutdown) {
//    final Thread unstarted = Thread.ofVirtual().unstarted(onShutdown);
    hook = new Hook(onShutdown);
    Runtime.getRuntime().addShutdownHook(hook);
  }

//  public void removeShutdownHook() {
//    if (hook != null) {
//      Runtime.getRuntime().removeShutdownHook(hook);
//    }
//  }

  static class Hook extends Thread {
    Hook(Runnable runnable) {
      super(runnable);
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
      if (status != newStatus) {
        //Status oldStatus = status;
        status = newStatus;
        //onChange(new Event(newStatus, oldStatus));
      }
    } finally {
      lock.unlock();
    }
  }

//  @Override
//  public void register(Listener listener) {
//    lock.lock();
//    try {
//      listeners.add(listener);
//    } finally {
//      lock.unlock();
//    }
//  }
//
//  private void onChange(Event event) {
//    for (Listener listener : listeners) {
//      listener.onChange(event);
//    }
//  }
//
//  private static class Event implements AppLifecycle.StatusChange {
//
//    final Status newStatus;
//    final Status oldStatus;
//
//    Event(Status newStatus, Status oldStatus) {
//      this.newStatus = newStatus;
//      this.oldStatus = oldStatus;
//    }
//
//    @Override
//    public Status newStatus() {
//      return newStatus;
//    }
//
//    @Override
//    public Status oldStatus() {
//      return oldStatus;
//    }
//  }
}
