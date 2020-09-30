package io.avaje.jex;

import io.avaje.jex.jetty.JettySpiServer;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.SpiServer;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;

public class Jex {

  private final Routing routing = new DefaultRouting();
  private final ErrorHandling errorHandling = new DefaultErrorHandling();
  private final StaticFileConfig staticFiles;

  public final Inner inner = new Inner();
  public final Jetty jetty = new Jetty();

  private Jex() {
    this.staticFiles = new DefaultStaticFileConfig(this);
  }

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
  }

  /**
   * Jetty specific configuration options.
   */
  public static class Jetty {
    public boolean sessions = true;
    public boolean security = true;
    public SessionHandler sessionHandler;
    public ServletContextHandler contextHandler;
    public org.eclipse.jetty.server.Server server;
  }

  public Jex errorHandling(ErrorHandling.Service service) {
    service.add(errorHandling);
    return this;
  }

  public ErrorHandling errorHandling() {
    return errorHandling;
  }

  /**
   * Add routes and handlers to the routing.
   */
  public Jex routing(Routing.Service routes) {
    routes.add(routing);
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
   * Start the server.
   */
  public Server start() {
    final Optional<SpiServer> server = ServiceLoader.load(SpiServer.class).findFirst();
    if (server.isEmpty()) {
      return new JettySpiServer().start(this);
    }
    return server.get().start(this);
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
