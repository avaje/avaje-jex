package io.avaje.jex;

import java.util.Collection;
import java.util.function.Consumer;

import io.avaje.inject.BeanScope;
import io.avaje.jex.Routing.HttpService;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.ExceptionHandler;
import io.avaje.jex.http.ExchangeHandler;
import io.avaje.jex.http.HttpFilter;
import io.avaje.jex.http.sse.SseClient;
import io.avaje.jex.http.sse.SseHandler;
import io.avaje.jex.security.Role;
import io.avaje.jex.spi.JexPlugin;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.TemplateRender;

/**
 * Create configure and start Jex.
 *
 * <pre>{@code
 * final Jex.Server app = Jex.create()
 *   .routing(routing -> routing
 *     .get("/", ctx -> ctx.text("hello world"))
 *     .get("/one", ctx -> ctx.text("one"))
 *   .port(8080)
 *   .start();
 *
 * app.shutdown();
 *
 * }</pre>
 */
public sealed interface Jex permits DJex {

  /**
   * Create Jex.
   *
   * <pre>{@code
   *
   * final Jex.Server app = Jex.create()
   *   .routing(routing -> routing
   *     .get("/", ctx -> ctx.text("hello world"))
   *     .get("/one", ctx -> ctx.text("one"))
   *   .port(8080)
   *   .start();
   *
   * app.shutdown();
   *
   * }</pre>
   */
  static Jex create() {
    return new DJex();
  }

  /**
   * Adds a new HTTP route and its associated handler to the Jex routing configuration.
   *
   * @param routes The HTTP service to add.
   */
  Jex routing(Routing.HttpService routes);

  /**
   * Adds multiple HTTP routes and their associated handlers to the Jex routing configuration.
   *
   * @param routes A collection of HTTP services to add.
   */
  Jex routing(Collection<Routing.HttpService> routes);

  /**
   * Returns the routing configuration object, allowing for further customization.
   *
   * @return The routing configuration object.
   */
  Routing routing();

  /**
   * Adds a GET handler to the route configuration.
   *
   * @param path The path pattern to match the request URI.
   * @param handler The handler to invoke when a GET request matches the path.
   * @param roles An array of roles that are associated with this endpoint.
   */
  default Jex get(String path, ExchangeHandler handler, Role... roles) {
    routing().get(path, handler, roles);
    return this;
  }

  /**
   * Adds a POST handler to the route configuration.
   *
   * @param path The path pattern to match the request URI.
   * @param handler The handler to invoke when a POST request matches the path.
   * @param roles An array of roles that are associated with this endpoint.
   */
  default Jex post(String path, ExchangeHandler handler, Role... roles) {
    routing().post(path, handler, roles);
    return this;
  }

  /**
   * Adds a PUT handler to the route configuration.
   *
   * @param path The path pattern to match the request URI.
   * @param handler The handler to invoke when a PUT request matches the path.
   * @param roles An array of roles that are associated with this endpoint.
   */
  default Jex put(String path, ExchangeHandler handler, Role... roles) {
    routing().put(path, handler, roles);
    return this;
  }

  /**
   * Adds a PATCH handler to the route configuration.
   *
   * @param path The path pattern to match the request URI.
   * @param handler The handler to invoke when a PATCH request matches the path.
   * @param roles An array of roles that are associated with this endpoint.
   */
  default Jex patch(String path, ExchangeHandler handler, Role... roles) {
    routing().patch(path, handler, roles);
    return this;
  }

  /**
   * Adds a DELETE handler to the route configuration.
   *
   * @param path The path pattern to match the request URI.
   * @param handler The handler to invoke when a DELETE request matches the path.
   * @param roles An array of roles that are associated with this endpoint.
   */
  default Jex delete(String path, ExchangeHandler handler, Role... roles) {
    routing().delete(path, handler, roles);
    return this;
  }

  /**
   * Adds an OPTIONS handler to the route configuration.
   *
   * @param path The path pattern to match the request URI.
   * @param handler The handler to invoke when an OPTIONS request matches the path.
   * @param roles An array of roles that are associated with this endpoint.
   */
  default Jex options(String path, ExchangeHandler handler, Role... roles) {
    routing().options(path, handler, roles);
    return this;
  }

  /**
   * Adds an SSE handler to the route configuration.
   *
   * @param path The path pattern to match the request URI.
   * @param handler The sse handler to invoke when a GET request matches the path.
   * @param roles An array of roles that are associated with this endpoint.
   */
  default Jex sse(String path, Consumer<SseClient> handler, Role... roles) {
    return get(path, new SseHandler(handler), roles);
  }

  /** Add a filter for all matched requests. */
  default Jex filter(HttpFilter handler) {
    routing().filter(handler);
    return this;
  }

  /** Add a pre-processing filter for all matched requests. */
  default Jex before(Consumer<Context> handler) {
    routing().before(handler);
    return this;
  }

  /** Add a post-processing filter for all matched requests. */
  default Jex after(Consumer<Context> handler) {
    routing().after(handler);
    return this;
  }

  /**
   * Registers an exception handler that handles the given type of exceptions. This will replace an
   * existing error handler for the same exception class.
   *
   * @param exceptionClass the type of exception to handle by this handler
   * @param handler the error handler
   * @param <T> exception type
   */
  default <T extends Exception> Jex error(Class<T> exceptionClass, ExceptionHandler<T> handler) {
    routing().error(exceptionClass, handler);
    return this;
  }

  /**
   * Add a group of route handlers with a common path prefix.
   *
   * <pre>{@code
   * routing.path("api", g -> {
   *     g.get("/", ctx -> ctx.text("apiRoot"));
   *     g.get("{id}", ctx -> ctx.text("api-" + ctx.pathParam("id")));
   * });
   *
   * }</pre>
   *
   * @param path the common path prefix
   * @param group the function to register the rout handlers
   *
   */
  default Jex group(String path, HttpService group) {
    routing().group(path, group);
    return this;
  }

  /**
   * Sets the JSON service to use for serialization and deserialization.
   *
   * @param jsonService The JSON service to use.
   */
  Jex jsonService(JsonService jsonService);

  /**
   * Adds a plugin to the Jex instance, extending its functionality.
   *
   * @param plugin The plugin to add.
   */
  Jex plugin(JexPlugin plugin);

  /**
   * Configures the Jex instance using a dependency injection scope from Avaje-Inject.
   *
   * <p>This method allows you to leverage the Avaje-Inject framework to provide dependencies like
   * Handlers, StaticResources, and Plugins to the Jex instance.
   *
   * @param beanScope The Avaje-Inject BeanScope containing the dependencies.
   * @return The configured Jex instance.
   */
  Jex configureWith(BeanScope beanScope);

  /**
   * Configures the Jex instance using a functional approach.
   *
   * <p>The provided consumer lambda allows you to customize the Jex configuration, such as setting
   * the port, compression, and other options.
   *
   * @param configure A consumer lambda that accepts a {@link JexConfig} instance for configuration.
   * @return The configured Jex instance.
   */
  Jex config(Consumer<JexConfig> configure);

  /**
   * Sets the port number on which the Jex server will listen for incoming requests.
   *
   * <p>The default value is 8080. If The port is set to 0, the server will randomly choose an available port.
   *
   * @param port The port number to use.
   */
  Jex port(int port);

  /**
   * Sets the context path for the Jex application.
   *
   * <p>The context path is the portion of the URL that identifies the application.
   *
   * @param contextPath The context path to use.
   * @return The updated Jex instance.
   */
  Jex contextPath(String contextPath);

  /**
   * Explicitly register a template renderer.
   *
   * <p>Note that if not explicitly registered TemplateRender's can be automatically registered via
   * ServiceLoader just by including them to the class path.
   *
   * @param renderer The template renderer to register
   * @param extensions The extensions the renderer is used for
   */
  Jex register(TemplateRender renderer, String... extensions);

  /** Return the application lifecycle support. */
  AppLifecycle lifecycle();

  /** Return the configuration. */
  JexConfig config();

  /** Start the server. */
  Jex.Server start();

  /** The running server. */
  interface Server {

    /**
     * Register a function to execute LAST on shutdown after all the normal lifecycle shutdown
     * functions have run.
     *
     * <p>Typically, we desire to shut down logging (e.g. Log4J) last.
     */
    void onShutdown(Runnable onShutdown);

    /** Shutdown the server. */
    void shutdown();

    /** The port of the server */
    int port();
  }
}
