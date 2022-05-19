package boot;

public interface BootJex {

  /**
   * Start Jex server using DI.
   */
  static void start() {
    BootJexState.start();
  }

  /**
   * Stop the Jex server.
   */
  static void stop() {
    BootJexState.stop();
  }

  static void restart() {
    BootJexState.restart();
  }
}
