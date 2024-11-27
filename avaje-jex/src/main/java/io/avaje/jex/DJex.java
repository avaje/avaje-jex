package io.avaje.jex;

import io.avaje.inject.BeanScope;
import io.avaje.jex.core.CoreServiceLoader;
import io.avaje.jex.core.CoreServiceManager;
import io.avaje.jex.core.HealthPlugin;
import io.avaje.jex.jdk.JdkServerStart;
import io.avaje.jex.routes.RoutesBuilder;
import io.avaje.jex.routes.SpiRoutes;
import io.avaje.jex.spi.*;

import java.util.*;
import java.util.function.Consumer;

final class DJex implements Jex {

  private final Routing routing = new DefaultRouting();
  private final AppLifecycle lifecycle = new DefaultLifecycle();
  private final Map<Class<?>, Object> attributes = new HashMap<>();
  private final DJexConfig config = new DJexConfig();

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
  public Jex routing(Routing.HttpService routes) {
    routing.add(routes);
    return this;
  }

  @Override
  public Jex routing(Collection<Routing.HttpService> routes) {
    routing.addAll(routes);
    return this;
  }

  @Override
  public Routing routing() {
    return routing;
  }

  @Override
  public Jex jsonService(JsonService jsonService) {
    config.jsonService(jsonService);
    return this;
  }

  @Override
  public Jex plugin(JexPlugin plugin) {
    plugin.apply(this);
    return this;
  }

  @Override
  public Jex configureWith(BeanScope beanScope) {
    lifecycle.onShutdown(beanScope::close);
    for (JexPlugin plugin : beanScope.list(JexPlugin.class)) {
      plugin.apply(this);
    }
    routing.addAll(beanScope.list(Routing.HttpService.class));
    beanScope.getOptional(JsonService.class).ifPresent(this::jsonService);
    return this;
  }

  @Override
  public Jex configure(Consumer<JexConfig> configure) {
    configure.accept(config);
    return this;
  }

  @Override
  public Jex port(int port) {
    config.port(port);
    return this;
  }

  @Override
  public Jex context(String contextPath) {
    config.contextPath(contextPath);
    return this;
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

    if (config.useSpiPlugins()) {
      CoreServiceLoader.plugins().forEach(p -> p.apply(this));
    }

    final SpiRoutes routes =
        new RoutesBuilder(routing, config.ignoreTrailingSlashes())
            .build();

    return new JdkServerStart().start(this, routes, CoreServiceManager.create(this));
  }
}
