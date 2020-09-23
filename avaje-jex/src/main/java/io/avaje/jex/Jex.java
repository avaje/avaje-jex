package io.avaje.jex;

import io.avaje.jex.core.JettyLaunch;

import java.util.function.Consumer;

public class Jex {

  private final Routing routing = new DefaultRouting();

  private final ErrorHandling errorHandling = new DefaultErrorHandling();

  private final JexConfig config = new JexConfig();

  private final StaticFileConfig staticFiles;

  private Jex() {
    this.staticFiles = new DefaultStaticFileConfig(this);
  }

  public static Jex create() {
    return new Jex();
  }

  public Jex errorHandling(ErrorHandling.Service service) {
    service.add(errorHandling);
    return this;
  }

  public ErrorHandling errorHandling() {
    return errorHandling;
  }

  public Jex routing(Routing.Service routes) {
    routes.add(routing);
    return this;
  }

  public Routing routing() {
    return routing;
  }

  public Jex config(Consumer<JexConfig> consumer) {
    consumer.accept(config);
    return this;
  }

  public JexConfig config() {
    return config;
  }

  /**
   * Set the port to use.
   */
  public Jex port(int port) {
    config.port(port);
    return this;
  }

  /**
   * Add an exception handler for the given exception type.
   */
  public <T extends Exception> Jex exception(Class<T> exceptionClass, ExceptionHandler<T> handler) {
    errorHandling.exception(exceptionClass, handler);
    return this;
  }

  /**
   * Start the server.
   */
  public Server start() {
    return new JettyLaunch(this).start();
  }

  public StaticFileConfig staticFiles() {
    return staticFiles;
  }

  /**
   * The running server.
   */
  public interface Server {

    void shutdown();
  }
}
