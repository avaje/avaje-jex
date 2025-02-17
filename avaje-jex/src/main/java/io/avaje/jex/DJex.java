package io.avaje.jex;

import io.avaje.inject.BeanScope;
import io.avaje.jex.core.BootstrapServer;
import io.avaje.jex.spi.*;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.spi.HttpServerProvider;

import java.util.*;
import java.util.function.Consumer;

final class DJex implements Jex {

  private final Routing routing = new DefaultRouting();
  private final AppLifecycle lifecycle = new DefaultLifecycle();
  private final DJexConfig config = new DJexConfig();

  @Override
  public DJexConfig config() {
    return config;
  }

  @Override
  public Jex config(Consumer<JexConfig> configure) {
    configure.accept(config);
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
    beanScope.getOptional(HttpsConfigurator.class).ifPresent(config()::httpsConfig);
    beanScope.getOptional(HttpServerProvider.class).ifPresent(config()::serverProvider);
    return this;
  }

  @Override
  public Jex contextPath(String contextPath) {
    config.contextPath(contextPath);
    return this;
  }

  @Override
  public Jex jsonService(JsonService jsonService) {
    config.jsonService(jsonService);
    return this;
  }

  @Override
  public AppLifecycle lifecycle() {
    return lifecycle;
  }

  @Override
  public Jex plugin(JexPlugin plugin) {
    plugin.apply(this);
    return this;
  }

  @Override
  public Jex port(int port) {
    config.port(port);
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
  public Routing routing() {
    return routing;
  }

  @Override
  public Jex routing(Collection<Routing.HttpService> routes) {
    routing.addAll(routes);
    return this;
  }

  @Override
  public Jex routing(Routing.HttpService routes) {
    routing.add(routes);
    return this;
  }

  @Override
  public Server start() {
    return BootstrapServer.start(this);
  }
}
