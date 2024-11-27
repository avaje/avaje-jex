package io.avaje.jex;

import io.avaje.config.Config;
import io.avaje.inject.BeanScope;

final class BootJexState {

  private static State state;

  static void start(BeanScope beanScope) {
    state = new BootJexState().create(beanScope);
  }

  static void stop() {
    state.stop();
  }

  static void restart() {
    state.restart();
  }

  State create(BeanScope beanScope) {
    Jex jex = beanScope.getOptional(Jex.class).orElse(Jex.create());
    jex.configureWith(beanScope);

    JexConfig config = jex.config();
    int port = config.port();
    config.port(Config.getInt("server.port", port));

    jex.lifecycle().onShutdown(beanScope::close);
    return new State(jex.start());
  }

  private static final class State {

    private final Jex.Server server;

    State(Jex.Server server) {
      this.server = server;
    }

    void stop() {
      server.shutdown();
    }

    public void restart() {
      server.restart();
    }
  }
}
