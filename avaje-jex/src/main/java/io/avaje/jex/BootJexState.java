package io.avaje.jex;

import io.avaje.config.Config;
import io.avaje.inject.BeanScope;

class BootJexState {

  private static State state;

  static void start() {
    state = new BootJexState().create();
  }

  static void stop() {
    state.stop();
  }

  static void restart() {
    state.restart();
  }

  State create() {
    BeanScope beanScope = BeanScope.builder().build();

    Jex jex = beanScope.getOptional(Jex.class).orElse(Jex.create());
    jex.configureWith(beanScope);

    JexConfig config = jex.config();
    int port = config.port();
    if (port == 7001) {
      config.port(Config.getInt("jex.port", port));
    }

    jex.lifecycle().onShutdown(beanScope::close);
    return new State(jex.start(), beanScope);
  }

  private static class State {

    private final Jex.Server server;
    private final BeanScope beanScope;

    State(Jex.Server server, BeanScope beanScope) {
      this.server = server;
      this.beanScope = beanScope;
    }

    void stop() {
      server.shutdown();
    }

    public void restart() {
      // CRaC based startup ...
      //beanScope.restart();
      server.restart();
    }
  }
}
