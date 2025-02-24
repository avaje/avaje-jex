package io.avaje.jex;

import java.util.Collection;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.spi.HttpServerProvider;

import io.avaje.inject.BeanScope;
import io.avaje.jex.Routing.HttpService;
import io.avaje.jex.core.BootstrapServer;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.ExceptionHandler;
import io.avaje.jex.http.ExchangeHandler;
import io.avaje.jex.http.HttpFilter;
import io.avaje.jex.http.sse.SseClient;
import io.avaje.jex.security.Role;
import io.avaje.jex.spi.JexPlugin;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.TemplateRender;

/**
 * Create, configure and start Jex.
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
public final class Jex {

  private final Routing routing = new DefaultRouting();
  private final AppLifecycle lifecycle = new AppLifecycle();
  private final JexConfig config = new JexConfig();

  private Jex() {}

  /**
   * Create Jex.
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
  public static Jex create() {
    return new Jex();
  }

  /**
   * Adds a new HTTP route and its associated handler to the Jex routing configuration.
   *
   * @param routes The HTTP service to add.
   */
  public Jex routing(Routing.HttpService routes) {
    routing.add(routes);
    return this;
  }

  /**
   * Adds multiple HTTP routes and their associated handlers to the Jex routing configuration.
   *
   * @param routes A collection of HTTP services to add.
   */
  public Jex routing(Collection<Routing.HttpService> routes) {
    routing.addAll(routes);
    return this;
  }

  /**
   * Returns the routing configuration object, allowing for further customization.
   *
   * @return The routing configuration object.
   */
  public Routing routing() {
    return routing;
  }

  /**
   * Adds a GET handler to the route configuration.
   *
   * @param path The path pattern to match the request URI.
   * @param handler The handler to invoke when a GET request matches the path.
   * @param roles An array of roles that are associated with this endpoint.
   */
  public Jex get(String path, ExchangeHandler handler, Role... roles) {
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
  public Jex post(String path, ExchangeHandler handler, Role... roles) {
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
  public Jex put(String path, ExchangeHandler handler, Role... roles) {
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
  public Jex patch(String path, ExchangeHandler handler, Role... roles) {
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
  public Jex delete(String path, ExchangeHandler handler, Role... roles) {
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
  public Jex options(String path, ExchangeHandler handler, Role... roles) {
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
  public Jex sse(String path, Consumer<SseClient> handler, Role... roles) {
    return get(path, SseClient.handler(handler), roles);
  }

  /** Add a filter for all matched requests. */
  public Jex filter(HttpFilter handler) {
    routing().filter(handler);
    return this;
  }

  /** Add a pre-processing filter for all matched requests. */
  public Jex before(Consumer<Context> handler) {
    routing().before(handler);
    return this;
  }

  /** Add a post-processing filter for all matched requests. */
  public Jex after(Consumer<Context> handler) {
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
  public <T extends Exception> Jex error(Class<T> exceptionClass, ExceptionHandler<T> handler) {
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
   */
  public Jex group(String path, HttpService group) {
    routing().group(path, group);
    return this;
  }

  /**
   * Sets the JSON service to use for serialization and deserialization.
   *
   * @param jsonService The JSON service to use.
   */
  public Jex jsonService(JsonService jsonService) {
    config.jsonService(jsonService);
    return this;
  }

  /**
   * Adds a plugin to the Jex instance, extending its functionality.
   *
   * @param plugin The plugin to add.
   */
  public Jex plugin(JexPlugin plugin) {
    plugin.apply(this);
    return this;
  }

  /**
   * Configures the Jex instance using a dependency injection scope from Avaje-Inject.
   *
   * <p>This method allows you to leverage the Avaje-Inject framework to provide dependencies like
   * Handlers, StaticResources, and Plugins to the Jex instance.
   *
   * @param beanScope The Avaje-Inject BeanScope containing the dependencies.
   * @return The configured Jex instance.
   */
  public Jex configureWith(BeanScope beanScope) {
    lifecycle.onShutdown(beanScope::close);
    for (JexPlugin plugin : beanScope.list(JexPlugin.class)) {
      plugin.apply(this);
    }
    routing.addAll(beanScope.list(Routing.HttpService.class));
    beanScope.getOptional(JsonService.class).ifPresent(this::jsonService);
    beanScope.getOptional(HttpsConfigurator.class).ifPresent(config()::httpsConfig);
    beanScope.getOptional(HttpServerProvider.class).ifPresent(config()::serverProvider);
    return this;
  }

  /**
   * Configures the Jex instance using a functional approach.
   *
   * <p>The provided consumer lambda allows you to customize the Jex configuration, such as setting
   * the port, compression, and other options.
   *
   * @param configure A consumer lambda that accepts a {@link JexConfig} instance for configuration.
   * @return The configured Jex instance.
   */
  public Jex config(Consumer<JexConfig> configure) {
    configure.accept(config);
    return this;
  }

  /**
   * Sets the port number on which the Jex server will listen for incoming requests.
   *
   * <p>The default value is 8080. If The port is set to 0, the server will randomly choose an
   * available port.
   *
   * @param port The port number to use.
   */
  public Jex port(int port) {
    config.port(port);
    return this;
  }

  /**
   * Sets the context path for the Jex application.
   *
   * <p>The context path is the portion of the URL that identifies the application.
   *
   * @param contextPath The context path to use.
   * @return The updated Jex instance.
   */
  public Jex contextPath(String contextPath) {
    config.contextPath(contextPath);
    return this;
  }

  /**
   * Explicitly register a template renderer.
   *
   * <p>Note that if not explicitly registered TemplateRender's can be automatically registered via
   * ServiceLoader just by including them to the class path.
   *
   * @param renderer The template renderer to register
   * @param extensions The extensions the renderer is used for
   */
  public Jex register(TemplateRender renderer, String... extensions) {
    for (String extension : extensions) {
      config.renderer(extension, renderer);
    }
    return this;
  }

  /** Return the application lifecycle support. */
  public AppLifecycle lifecycle() {
    return lifecycle;
  }

  /** Return the configuration. */
  public JexConfig config() {
    return config;
  }

  /** Start the server. */
  public Server start() {
    return BootstrapServer.start(this);
  }

  /** The running server. */
  public interface Server {

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
