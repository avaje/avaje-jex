package io.avaje.jex;

import io.avaje.inject.BeanScope;
import io.avaje.jex.core.HealthPlugin;
import io.avaje.jex.spi.*;

import java.util.*;
import java.util.function.Consumer;

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
public class Jex {

  private final Routing routing = new DefaultRouting();
  private final ErrorHandling errorHandling = new DefaultErrorHandling();
  private final AppLifecycle lifecycle = new DefaultLifecycle();
  private final StaticFileConfig staticFiles;
  private final Map<Class<?>, Object> attributes = new HashMap<>();

  public final Config config = new Config();
  private ServerConfig serverConfig;

  private Jex() {
    this.staticFiles = new DefaultStaticFileConfig(this);
  }

  /**
   * Create Jex to configure with routes etc before starting.
   */
  public static Jex create() {
    return new Jex();
  }

  /**
   * Set a custom attribute that can be used by an implementation.
   */
  public <T> Jex attribute(Class<T> cls, T instance) {
    attributes.put(cls, instance);
    return this;
  }

  /**
   * Return a custom attribute.
   */
  @SuppressWarnings("unchecked")
  public <T> T attribute(Class<T> cls) {
    return (T) attributes.get(cls);
  }

  public static class Config {
    public int port = 7001;
    public String host;
    public String contextPath = "/";
    /**
     * By default include the HealthPlugin.
     */
    public boolean health = true;
    public boolean prefer405 = true;
    public boolean ignoreTrailingSlashes = true;

    public boolean preCompressStaticFiles;
    public JsonService jsonService;
    public AccessManager accessManager;
    public UploadConfig multipartConfig;
    public int multipartFileThreshold = 8 * 1024;
    public final Map<String, TemplateRender> renderers = new HashMap<>();
  }


  /**
   * Configure error handlers.
   */
  public Jex errorHandling(ErrorHandling.Service service) {
    service.add(errorHandling);
    return this;
  }

  /**
   * Return the Error handler to add error handlers.
   */
  public ErrorHandling errorHandling() {
    return errorHandling;
  }

  /**
   * Return the server specific configuration.
   */
  public ServerConfig serverConfig() {
    return serverConfig;
  }

  /**
   * Set the server specific configuration.
   */
  public Jex serverConfig(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;
    return this;
  }

  /**
   * Add routes and handlers to the routing.
   */
  public Jex routing(Routing.Service routes) {
    routing.add(routes);
    return this;
  }

  /**
   * Add many routes and handlers to the routing.
   */
  public Jex routing(Collection<Routing.Service> routes) {
    routing.addAll(routes);
    return this;
  }

  /**
   * Return the Routing to configure.
   */
  public Routing routing() {
    return routing;
  }

  /**
   * Set the AccessManager.
   */
  public Jex accessManager(AccessManager accessManager) {
    this.config.accessManager = accessManager;
    return this;
  }

  /**
   * Set the JsonService.
   */
  public Jex jsonService(JsonService jsonService) {
    this.config.jsonService = jsonService;
    return this;
  }

  /**
   * Add Plugin functionality.
   */
  public Jex plugin(Plugin plugin) {
    plugin.apply(this);
    return this;
  }

  /**
   * Configure given the dependency injection scope from <em>avaje-inject</em>.
   *
   * @param beanScope The scope potentially containing Handlers, AccessManager, Plugins etc.
   */
  public Jex configureWith(BeanScope beanScope) {
    lifecycle.onShutdown(beanScope::close);
    final AccessManager accessManager = beanScope.get(AccessManager.class);
    if (accessManager != null) {
      accessManager(accessManager);
    }
    for (Plugin plugin : beanScope.list(Plugin.class)) {
      plugin.apply(this);
    }
    routing.addAll(beanScope.list(Routing.Service.class));
    return this;
  }

  /**
   * Configure via a lambda taking the jex instance.
   */
  public Jex configure(Consumer<Jex> configure) {
    configure.accept(this);
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
   * Set the port to use.
   */
  public Jex port(int port) {
    this.config.port = port;
    return this;
  }

  /**
   * Set the context path.
   */
  public Jex context(String contextPath) {
    this.config.contextPath = contextPath;
    return this;
  }

  /**
   * Return the static file configuration.
   */
  public StaticFileConfig staticFiles() {
    return staticFiles;
  }

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
  public Jex register(TemplateRender renderer, String... extensions) {
    for (String extension : extensions) {
      config.renderers.put(extension, renderer);
    }
    return this;
  }

  /**
   * Start the server.
   */
  public Server start() {
    if (config.health) {
      plugin(new HealthPlugin());
    }
    final SpiRoutes routes = ServiceLoader.load(SpiRoutesProvider.class)
      .findFirst().get()
      .create(this.routing, this.config.accessManager, this.config.ignoreTrailingSlashes);

    final SpiServiceManager serviceManager = ServiceLoader.load(SpiServiceManagerProvider.class)
      .findFirst().get()
      .create(this);

    final Optional<SpiStartServer> start = ServiceLoader.load(SpiStartServer.class).findFirst();
    if (start.isEmpty()) {
      throw new IllegalStateException("There is no SpiStartServer? Missing dependency on jex-jetty?");
    }
    return start.get().start(this, routes, serviceManager);
  }

  /**
   * Return the application lifecycle support.
   */
  public AppLifecycle lifecycle() {
    return lifecycle;
  }

  /**
   * The running server.
   */
  public interface Server {

    /**
     * Shutdown the server.
     */
    void shutdown();
  }
}
