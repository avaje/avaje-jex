package io.avaje.jex;

import io.avaje.config.Config;
import io.avaje.inject.BeanScope;
import io.avaje.jex.Jex.Server;

/**
 * Start Jex using avaje-inject, avaje-http, avaje-config.
 *
 * <p>- avaje-http generates the adapter for the {@code @Controller}
 *
 * <p>- avaje-inject generates dependency injection wiring
 *
 * <p>- avaje-config reads external configuration.
 */
public interface AvajeJex {

  /**
   * Start Jex server using {@code @Controller} with avaje-inject, avaje-http, avaje-config.
   *
   * <pre>{@code
   * public static void main(String[] args) {
   *
   *   AvajeJex.start();
   * }
   * }</pre>
   *
   * @return The running server.
   */
  static Server start() {
    return start(BeanScope.builder().build());
  }

  /**
   * Start Jex server using {@code @Controller} with avaje-inject, avaje-http, avaje-config.
   *
   * <pre>{@code
   * public static void main(String[] args) {
   *
   *   AvajeJex.start();
   * }
   * }</pre>
   *
   * @param beanScope the beanscope used to configure Jex
   * @return The running server.
   */
  static Server start(BeanScope beanScope) {
    Jex jex = beanScope.getOptional(Jex.class).orElse(Jex.create());
    jex.configureWith(beanScope);

    JexConfig config = jex.config();
    config.port(Config.getInt("server.port", config.port()));
    config.contextPath(Config.get("server.context.path", config.contextPath()));
    config.host(Config.get("server.context.host", config.host()));

    return jex.start();
  }
}
