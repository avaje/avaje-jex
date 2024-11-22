package io.avaje.jex;

import io.avaje.inject.BeanScope;

/**
 * Start Jex using {@code @Controller} and avaje-inject, avaje-http, avaje-config.
 * <p>
 * - avaje-http generates the adapter for the {@code @Controller}
 * - avaje-inject generates dependency injection wiring
 * - avaje-config reads external configuration (application.properties|yaml, application-test.properties|yaml).
 */
public interface BootJex {

  /**
   * Start Jex server using {@code @Controller} with avaje-inject, avaje-http, avaje-config.
   *
   * <pre>{@code
   * public static void main(String[] args) {
   *
   *   BootJex.start();
   * }
   * }</pre>
   */
  static void start() {
    BootJexState.start(BeanScope.builder().build());
  }

  /**
   * Start Jex server using {@code @Controller} with avaje-inject, avaje-http, avaje-config.
   *
   * <pre>{@code
   * public static void main(String[] args) {
   *
   *   BootJex.start();
   * }
   * }</pre>
   */
  static void start(BeanScope beanScope) {
    BootJexState.start(beanScope);
  }
}
