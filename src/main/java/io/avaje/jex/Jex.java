package io.avaje.jex;

import io.avaje.jex.core.JettyLaunch;

import java.util.function.Consumer;

public class Jex {

  private final Routing routing = new DefaultRouting();

  private final JexConfig config = new JexConfig();

  private Jex() {
    // hide
  }

  public static Jex create() {
    return new Jex();
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

  public Jex port(int port) {
    config.port(port);
    return this;
  }

  public Server start() {
    return new JettyLaunch(this).start();
  }

  /**
   * The running server.
   */
  public interface Server {

    void shutdown();
  }
}
