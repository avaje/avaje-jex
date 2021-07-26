package io.avaje.jex;

/**
 * Application lifecycle support.
 */
public interface AppLifecycle {

  enum Status {
    STARTING,
    STARTED,
    STOPPING,
    STOPPED
  }

  /**
   * Register a Runnable to run on shutdown of the server.
   * <p>
   * This will execute after the server has deemed there are no active requests.
   */
  void onShutdown(Runnable onShutdown);

  /**
   * Register the runnable with the Runtime as a shutdown hook.
   */
  void registerShutdownHook(Runnable onShutdown);

  /**
   * Return the current status.
   */
  Status status();

  /**
   * Set the current status.
   */
  void status(Status newStatus);

  /**
   * Return true if status starting or started (the server is coming up).
   */
  default boolean isAlive() {
    Status status = status();
    return status == Status.STARTING || status == Status.STARTED;
  }

  /**
   * Return true the server has started.
   */
  default boolean isReady() {
    Status status = status();
    return status == Status.STARTED;
  }

//  void register(Listener listener);
//
//  interface Listener {
//    void onChange(StatusChange change);
//  }
//
//  interface StatusChange {
//    Status newStatus();
//    Status oldStatus();
//  }
}
