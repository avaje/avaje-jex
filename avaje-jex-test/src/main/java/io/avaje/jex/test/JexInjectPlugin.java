package io.avaje.jex.test;

import io.avaje.http.client.HttpClientContext;
import io.avaje.inject.BeanScope;
import io.avaje.inject.test.Plugin;
import io.avaje.jex.Jex;

import java.lang.annotation.Annotation;

/**
 * avaje-inject-test plugin that:
 *
 * - Detects when a http client is being used in a test
 * - Starts Jex server on random port
 * - Creates the appropriate client for the port (to be injected into the test class)
 * - Shutdown the server on test completion (Plugin.Scope close)
 */
public final class JexInjectPlugin implements Plugin {

  private static final String AVAJE_HTTP_CLIENT = "io.avaje.http.api.Client";
  private static final String AVAJE_HTTP_PATH = "io.avaje.http.api.Path";

  /**
   * Return true if it's a http client this plugin supports.
   */
  @Override
  public boolean forType(Class<?> type) {
    return HttpClientContext.class.equals(type) || isHttpClientApi(type);
  }

  private boolean isHttpClientApi(Class<?> type) {
    if (!type.isInterface()) {
      return false;
    }
    for (Annotation annotation : type.getAnnotations()) {
      String name = annotation.annotationType().getName();
      if (AVAJE_HTTP_CLIENT.equals(name)) {
        return true;
      }
      if (AVAJE_HTTP_PATH.equals(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Create a scope.
   * <p>
   * The scope will contain a server + client pair.
   */
  @Override
  public Scope createScope(BeanScope beanScope) {
    return new LocalScope(beanScope);
  }

  private static class LocalScope implements Plugin.Scope {

    private final int port;
    private final Jex.Server server;
    private final HttpClientContext.Builder httpClientBuilder;
    private HttpClientContext httpClient;

    LocalScope(BeanScope beanScope) {
      Jex jex = beanScope.getOptional(Jex.class)
        .orElse(Jex.create())
        .configureWith(beanScope)
        .port(0);

      // get a HttpClientContext.Builder provided by dependency injection test scope or new one up
      this.httpClientBuilder = beanScope.getOptional(HttpClientContext.Builder.class).orElse(HttpClientContext.newBuilder());
      this.server = jex.start();
      this.port = server.port();
    }

    @Override
    public Object create(Class<?> type) {
      if (HttpClientContext.class.equals(type)) {
        return clientContext();
      }
      return apiClient(type);
    }

    private Object apiClient(Class<?> clientInterface) {
      return clientContext().create(clientInterface);
    }

    private HttpClientContext clientContext() {
      if (httpClient == null) {
        httpClient = httpClientBuilder
          .baseUrl("http://localhost:" + port)
          .build();
      }
      return httpClient;
    }

    @Override
    public void close() {
      server.shutdown();
    }
  }
}
