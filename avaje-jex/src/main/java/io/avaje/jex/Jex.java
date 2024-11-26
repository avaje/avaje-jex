package io.avaje.jex;

import java.util.Collection;
import java.util.function.Consumer;

import io.avaje.inject.BeanScope;
import io.avaje.jex.spi.JexPlugin;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.TemplateRender;

/**
 * Create configure and start Jex.
 *
 * <pre>{@code
 *
 *     final Jex.Server app = Jex.create()
 *       .routing(routing -> routing
 *         .get("/", ctx -> ctx.text("hello world"))
 *         .get("/one", ctx -> ctx.text("one"))
 *       .port(8080)
 *       .start();
 *
 *     app.shutdown();
 *
 * }</pre>
 */
public sealed interface Jex permits DJex {

  /**
   * Create Jex.
   *
   * <pre>{@code
   *
   *     final Jex.Server app = Jex.create()
   *       .routing(routing -> routing
   *         .get("/", ctx -> ctx.text("hello world"))
   *         .get("/one", ctx -> ctx.text("one"))
   *       .port(8080)
   *       .start();
   *
   *     app.shutdown();
   *
   * }</pre>
   */
  static Jex create() {
    return new DJex();
  }

  /**
   * Set a custom attribute that can be used by an implementation.
   */
  <T> Jex attribute(Class<T> cls, T instance);

  /**
   * Return a custom attribute.
   */
  <T> T attribute(Class<T> cls);

  /**
   * Add routes and handlers to the routing.
   */
  Jex routing(Routing.HttpService routes);

  /**
   * Add many routes and handlers to the routing.
   */
  Jex routing(Collection<Routing.HttpService> routes);

  /**
   * Return the Routing to configure.
   */
  Routing routing();

  /** Add a static resource route */
  default Jex staticResource(StaticContentConfig config) {
    routing().get(config.httpPath(), config.createHandler());
    return this;
  }

  /** Add a static resource route using a consumer */
  default Jex staticResource(Consumer<StaticContentConfig> consumer) {
    var builder = StaticResourceHandlerBuilder.builder();
    consumer.accept(builder);

    return staticResource(builder);
  }

  /**
   * Set the JsonService.
   */
  Jex jsonService(JsonService jsonService);

  /**
   * Add Plugin functionality.
   */
  Jex plugin(JexPlugin plugin);

  /**
   * Configure given the dependency injection scope from <em>avaje-inject</em>.
   *
   * @param beanScope The scope potentially containing Handlers, AccessManager, Plugins etc.
   */
  Jex configureWith(BeanScope beanScope);

  /**
   * Configure via a lambda taking the JexConfig instance.
   */
  Jex configure(Consumer<JexConfig> configure);

  /**
   * Set the port to use.
   */
  Jex port(int port);

  /**
   * Set the context path.
   */
  Jex context(String contextPath);

  /**
   * Explicitly register a template renderer.
   * <p>
   * Note that if not explicitly registered TemplateRender's can be
   * automatically registered via ServiceLoader just by including them
   * to the class path.
   *
   * @param renderer   The template renderer to register
   * @param extensions The extensions the renderer is used for
   */
  Jex register(TemplateRender renderer, String... extensions);

  /**
   * Return the application lifecycle support.
   */
  AppLifecycle lifecycle();

  /**
   * Return the configuration.
   */
  JexConfig config();

  /**
   * Start the server.
   */
  Jex.Server start();

  /**
   * The running server.
   */
  interface Server {

    /**
     * Register a function to execute LAST on shutdown after all the
     * normal lifecycle shutdown functions have run.
     * <p>
     * Typically, we desire to shut down logging (e.g. Log4J) last.
     */
    void onShutdown(Runnable onShutdown);

    /**
     * Shutdown the server.
     */
    void shutdown();

    /**
     * Return the port the server is using.
     */
    default int port() {
      throw new IllegalStateException("not supported");
    }

    default void restart() {
      throw new IllegalStateException("not supported");
    }
  }
}
