package io.avaje.jex;

import io.avaje.inject.BeanScope;
import io.avaje.jex.core.HealthPlugin;
import io.avaje.jex.spi.*;

import java.util.*;
import java.util.function.Consumer;

final class DJex implements Jex {

  private final Routing routing = new DefaultRouting();
  private final ErrorHandling errorHandling = new DefaultErrorHandling();
  private final AppLifecycle lifecycle = new DefaultLifecycle();
  private final StaticFileConfig staticFiles;
  private final Map<Class<?>, Object> attributes = new HashMap<>();
  private final DJexConfig config = new DJexConfig();
  private ServerConfig serverConfig;

  DJex() {
    this.staticFiles = new DefaultStaticFileConfig(this);
  }

  @Override
  public DJexConfig config() {
    return config;
  }


  @Override
  public <T> Jex attribute(Class<T> cls, T instance) {
    attributes.put(cls, instance);
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T attribute(Class<T> cls) {
    return (T) attributes.get(cls);
  }

  @Override
  public Jex errorHandling(ErrorHandling.Service service) {
    service.add(errorHandling);
    return this;
  }

  @Override
  public ErrorHandling errorHandling() {
    return errorHandling;
  }

  @Override
  public ServerConfig serverConfig() {
    return serverConfig;
  }

  @Override
  public Jex serverConfig(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;
    return this;
  }

  @Override
  public Jex routing(Routing.Service routes) {
    routing.add(routes);
    return this;
  }

  @Override
  public Jex routing(Collection<Routing.Service> routes) {
    routing.addAll(routes);
    return this;
  }

  @Override
  public Routing routing() {
    return routing;
  }

  @Override
  public Jex accessManager(AccessManager accessManager) {
    this.config.accessManager(accessManager);
    return this;
  }

  @Override
  public Jex jsonService(JsonService jsonService) {
    this.config.jsonService(jsonService);
    return this;
  }

  @Override
  public Jex plugin(Plugin plugin) {
    plugin.apply(this);
    return this;
  }

  @Override
  public Jex configureWith(BeanScope beanScope) {
    lifecycle.onShutdown(beanScope::close);
    beanScope.getOptional(AccessManager.class).ifPresent(this::accessManager);
    for (Plugin plugin : beanScope.list(Plugin.class)) {
      plugin.apply(this);
    }
    routing.addAll(beanScope.list(Routing.Service.class));
    return this;
  }

  @Override
  public Jex configure(Consumer<Jex> configure) {
    configure.accept(this);
    return this;
  }

  @Override
  public <T extends Exception> Jex exception(Class<T> exceptionClass, ExceptionHandler<T> handler) {
    errorHandling.exception(exceptionClass, handler);
    return this;
  }

  @Override
  public Jex port(int port) {
    this.config.port(port);
    return this;
  }

  @Override
  public Jex context(String contextPath) {
    this.config.contextPath(contextPath);
    return this;
  }

  @Override
  public StaticFileConfig staticFiles() {
    return staticFiles;
  }

  @Override
  public Jex register(TemplateRender renderer, String... extensions) {
    for (String extension : extensions) {
      config.renderer(extension, renderer);
    }
    return this;
  }

  @Override
  public AppLifecycle lifecycle() {
    return lifecycle;
  }

  @Override
  public Server start() {
    if (config.health()) {
      plugin(new HealthPlugin());
    }
    final SpiRoutes routes = ServiceLoader.load(SpiRoutesProvider.class)
      .findFirst().get()
      .create(this.routing, this.config.accessManager(), this.config.ignoreTrailingSlashes());

    final SpiServiceManager serviceManager = ServiceLoader.load(SpiServiceManagerProvider.class)
      .findFirst().get()
      .create(this);

    final Optional<SpiStartServer> start = ServiceLoader.load(SpiStartServer.class).findFirst();
    if (start.isEmpty()) {
      throw new IllegalStateException("There is no SpiStartServer? Missing dependency on jex-jetty?");
    }
    return start.get().start(this, routes, serviceManager);
  }

}
