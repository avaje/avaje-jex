package io.avaje.jex;

import io.avaje.jex.jetty.JettyStartServer;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.SpiStartServer;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import jakarta.servlet.MultipartConfigElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
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
  private final StaticFileConfig staticFiles;

  public final Inner inner = new Inner();
  public final Jetty jetty = new Jetty();

  private Jex() {
    this.staticFiles = new DefaultStaticFileConfig(this);
  }

  /**
   * Create Jex to configure with routes etc before starting.
   */
  public static Jex create() {
    return new Jex();
  }

  public static class Inner {
    public int port = 7001;
    public String contextPath = "/";
    public boolean prefer405 = true;
    public boolean ignoreTrailingSlashes = true;

    public boolean preCompressStaticFiles;
    public JsonService jsonService;
    public AccessManager accessManager;
    public MultipartConfigElement multipartConfig;
    public int multipartFileThreshold = 8 * 1024;
    public final Map<String, TemplateRender> renderers = new HashMap<>();
  }

  /**
   * Jetty specific configuration options.
   */
  public static class Jetty {
    public boolean sessions = true;
    public boolean security = true;
    /**
     * Set true to use Loom virtual threads for ThreadPool.
     * This requires JDK 17 with Loom included.
     */
    public boolean virtualThreads;
    /**
     * Set maxThreads when using default QueuedThreadPool. Defaults to 200.
     */
    public int maxThreads;
    public SessionHandler sessionHandler;
    public ServletContextHandler contextHandler;
    public org.eclipse.jetty.server.Server server;
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

  /***
   * Set the AccessManager.
   */
  public Jex accessManager(AccessManager accessManager) {
    this.inner.accessManager = accessManager;
    return this;
  }

  /***
   * Set the JsonService.
   */
  public Jex jsonService(JsonService jsonService) {
    this.inner.jsonService = jsonService;
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
    this.inner.port = port;
    return this;
  }

  /**
   * Set the context path.
   */
  public Jex context(String contextPath) {
    this.inner.contextPath = contextPath;
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
      inner.renderers.put(extension, renderer);
    }
    return this;
  }

  /**
   * Start the server.
   */
  public Server start() {
    final Optional<SpiStartServer> start = ServiceLoader.load(SpiStartServer.class).findFirst();
    if (start.isEmpty()) {
      return new JettyStartServer().start(this);
    }
    return start.get().start(this);
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
