package io.avaje.jex;

/** Defines the lifecycle configuration for an application. */
public interface AppLifecycle {

  /** Represents the possible states of the application server. */
  enum Status {
    STARTING,
    STARTED,
    STOPPING,
    STOPPED
  }

  /**
   * Registers a runnable to be executed when the application server is shutting down.
   *
   * <p>This runnable will be executed after all active requests have been processed.
   *
   * @param onShutdown The runnable to execute on shutdown.
   */
  void onShutdown(Runnable onShutdown);

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
  void onShutdown(Runnable onShutdown, int order);

  /**
   * Registers a runnable as a shutdown hook with the JVM.
   *
   * <p>This runnable will be executed when the JVM is shutting down, regardless of the application
   * server's state.
   *
   * @param onShutdown The runnable to register as a shutdown hook.
   */
  void registerShutdownHook(Runnable onShutdown);

  /**
   * Returns the current status of the application server.
   *
   * @return The current status of the server.
   */
  Status status();

  /**
   * Sets the current status of the application server.
   *
   * @param newStatus The new status to set.
   */
  void status(Status newStatus);

  /**
   * Indicates whether the application server is currently starting or has started.
   *
   * @return true if the server is starting or started, false otherwise.
   */
  default boolean isAlive() {
    Status status = status();
    return status == Status.STARTING || status == Status.STARTED;
  }

  /**
   * Indicates whether the application server has fully started.
   *
   * @return true if the server has started, false otherwise.
   */
  default boolean isReady() {
    Status status = status();
    return status == Status.STARTED;
  }
}
