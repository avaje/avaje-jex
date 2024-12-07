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

  State create(BeanScope beanScope) {
    Jex jex = beanScope.getOptional(Jex.class).orElse(Jex.create());
    jex.configureWith(beanScope);

    JexConfig config = jex.config();
    config.port(Config.getInt("server.port", config.port()));
    config.contextPath(Config.get("server.context.path", config.contextPath()));
    config.host(Config.get("server.context.host", config.host()));
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
  }
}
