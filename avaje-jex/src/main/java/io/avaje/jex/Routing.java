package io.avaje.jex;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import io.avaje.jex.http.Context;
import io.avaje.jex.http.ExceptionHandler;
import io.avaje.jex.http.ExchangeHandler;
import io.avaje.jex.http.HttpFilter;
import io.avaje.jex.security.Role;

/** Routing abstraction. */
public sealed interface Routing permits DefaultRouting {

  /** Add the routes provided by the given HttpService. */
  Routing add(Routing.HttpService service);

  /** Add all the routes provided by the Routing Services. */
  Routing addAll(Collection<Routing.HttpService> routes);

  /**
   * Registers an exception handler that handles the given type of exceptions. This will replace an
   * existing error handler for the same exception class.
   *
   * @param exceptionClass the type of exception to handle by this handler
   * @param handler the error handler
   * @param <T> exception type
   */
  <T extends Exception> Routing error(Class<T> exceptionClass, ExceptionHandler<T> handler);

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
  Routing group(String path, HttpService group);

  /**
   * Adds a HEAD handler to the route configuration.
   *
   * @param path The path pattern to match the request URI.
   * @param handler The handler to invoke when a HEAD request matches the path.
   * @param roles roles that are associated with this endpoint.
   */
  Routing head(String path, ExchangeHandler handler, Role... roles);

  /**
   * Adds a GET handler to the route configuration.
   *
   * @param path The path pattern to match the request URI.
   * @param handler The handler to invoke when a GET request matches the path.
   * @param roles roles that are associated with this endpoint.
   */
  Routing get(String path, ExchangeHandler handler, Role... roles);

  /**
   * Adds a POST handler to the route configuration.
   *
   * @param path The path pattern to match the request URI.
   * @param handler The handler to invoke when a POST request matches the path.
   * @param roles roles that are associated with this endpoint.
   */
  Routing post(String path, ExchangeHandler handler, Role... roles);

  /**
   * Adds a PUT handler to the route configuration.
   *
   * @param path The path pattern to match the request URI.
   * @param handler The handler to invoke when a PUT request matches the path.
   * @param roles roles that are associated with this endpoint.
   */
  Routing put(String path, ExchangeHandler handler, Role... roles);

  /**
   * Adds a PATCH handler to the route configuration.
   *
   * @param path The path pattern to match the request URI.
   * @param handler The handler to invoke when a PATCH request matches the path.
   * @param roles roles that are associated with this endpoint.
   */
  Routing patch(String path, ExchangeHandler handler, Role... roles);

  /**
   * Adds a DELETE handler to the route configuration.
   *
   * @param path The path pattern to match the request URI.
   * @param handler The handler to invoke when a DELETE request matches the path.
   * @param roles roles that are associated with this endpoint.
   */
  Routing delete(String path, ExchangeHandler handler, Role... roles);

  /**
   * Adds a TRACE handler to the route configuration.
   *
   * @param path The path pattern to match the request URI.
   * @param handler The handler to invoke when a TRACE request matches the path.
   * @param roles roles that are associated with this endpoint.
   */
  Routing trace(String path, ExchangeHandler handler, Role... roles);

  /**
   * Adds an OPTIONS handler to the route configuration.
   *
   * @param path The path pattern to match the request URI.
   * @param handler The handler to invoke when an OPTIONS request matches the path.
   * @param roles roles that are associated with this endpoint.
   */
  Routing options(String path, ExchangeHandler handler, Role... roles);

  /** Add a filter for all matched requests. */
  Routing filter(HttpFilter handler);

  /** Add a pre-processing filter for all matched requests. */
  default Routing before(Consumer<Context> handler) {
    return filter(
        (ctx, chain) -> {
          handler.accept(ctx);
          chain.proceed();
        });
  }

  /** Add a post-processing filter for all matched requests. */
  default Routing after(Consumer<Context> handler) {
    return filter(
        (ctx, chain) -> {
          chain.proceed();
          handler.accept(ctx);
        });
  }

  /** Return all the registered handlers. */
  List<Entry> handlers();

  /** Return all the registered filters. */
  List<HttpFilter> filters();

  /** Return all the registered Exception Handlers. */
  Map<Class<?>, ExceptionHandler<?>> errorHandlers();

  /** Adds to the Routing. */
  @FunctionalInterface
  interface HttpService {

    /**
     * Add to the routing.
     *
     * @param routing The routing to add handlers to.
     */
    void add(Routing routing);
  }

  /** A routing entry. */
  interface Entry {

    /** Return the type of entry. */
    Type getType();

    /** Return the full path of the entry. */
    String getPath();

    /** Return the handler. */
    ExchangeHandler getHandler();

    /** Return the roles. */
    Set<Role> getRoles();
  }

  /** The type of route entry. */
  enum Type {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    HEAD,
    TRACE,
    OPTIONS;
  }
}
