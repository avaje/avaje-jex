package io.avaje.jex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 *
 */
class DefaultRouting implements Routing {

  private final List<Routing.Entry> handlers = new ArrayList<>();
  private final Deque<String> pathDeque = new ArrayDeque<>();

  DefaultRouting() {
    // hide
  }

  @Override
  public List<Routing.Entry> all() {
    return handlers;
  }

  private String path(String path) {
    return String.join("", pathDeque) + ((path.startsWith("/") || path.isEmpty()) ? path : "/" + path);
  }

  private void addEndpoints(String path, Group group) {
    path = path.startsWith("/") ? path : "/" + path;
    pathDeque.addLast(path);
    group.addGroup();
    //routes.accept(this);
    pathDeque.removeLast();
  }

  @Override
  public Routing path(String path, Group group) {
    addEndpoints(path, group);
    return this;
  }

//    /**
//     * Adds an exception mapper to the instance.
//     *
//     * @see <a href="https://javalin.io/documentation#exception-mapping">Exception mapping in docs</a>
//     */
//    public <T extends Exception> Javalin exception(@NotNull Class<T> exceptionClass, @NotNull ExceptionHandler<? super T> exceptionHandler) {
//        servlet.getExceptionMapper().getHandlers().put(exceptionClass, (ExceptionHandler<Exception>) exceptionHandler);
//        return this;
//    }

//    /**
//     * Adds an error mapper to the instance.
//     * Useful for turning error-codes (404, 500) into standardized messages/pages
//     *
//     * @see <a href="https://javalin.io/documentation#error-mapping">Error mapping in docs</a>
//     */
//    public ApiBuilder error(int statusCode, @NotNull Handler handler) {
//        servlet.getErrorMapper().getErrorHandlerMap().put(statusCode, handler);
//        return this;
//    }
//
//    /**
//     * Adds an error mapper for the specified content-type to the instance.
//     * Useful for turning error-codes (404, 500) into standardized messages/pages
//     *
//     * @see <a href="https://javalin.io/documentation#error-mapping">Error mapping in docs</a>
//     */
//    public Javalin error(int statusCode, @NotNull String contentType, @NotNull Handler handler) {
//        return error(statusCode, ErrorMapperKt.contentTypeWrap(contentType, handler));
//    }


  private void add(Type verb, String path, Handler handler, Set<Role> roles) {
    handlers.add(new Entry(verb, path(path), handler, roles));
  }

  private void addBefore(String path, Handler handler) {
    handlers.add(new Entry(path(path), handler));
  }

  private void addAfter(String path, Handler handler) {
    handlers.add(new Entry(path(path), handler));
  }

  // ********************************************************************************************
  // HTTP verbs
  // ********************************************************************************************

  @Override
  public Routing get(String path, Handler handler, Set<Role> permittedRoles) {
    add(Type.GET, path, handler, permittedRoles);
    return this;
  }

  @Override
  public Routing get(String path, Handler handler) {
    get(path, handler, Collections.emptySet());
    return this;
  }

  @Override
  public Routing get(Handler handler) {
    get("", handler);
    return this;
  }

  @Override
  public Routing get(Handler handler, Set<Role> permittedRoles) {
    get("", handler, permittedRoles);
    return this;
  }

  @Override
  public Routing post(String path, Handler handler, Set<Role> permittedRoles) {
    add(Type.POST, path, handler, permittedRoles);
    return this;
  }

  @Override
  public Routing post(String path, Handler handler) {
    post(path, handler, Collections.emptySet());
    return this;
  }

  @Override
  public Routing post(Handler handler) {
    post("", handler);
    return this;
  }

  @Override
  public Routing post(Handler handler, Set<Role> permittedRoles) {
    post("", handler, permittedRoles);
    return this;
  }

  // ********************************************************************************************
  // Before/after handlers (filters)
  // ********************************************************************************************

  @Override
  public Routing before(String path, Handler handler) {
    addBefore(path, handler);
    return this;
  }

  @Override
  public Routing before(Handler handler) {
    before("/*", handler);
    return this;
  }

  @Override
  public Routing after(String path, Handler handler) {
    addAfter(path, handler);
    return this;
  }

  @Override
  public Routing after(Handler handler) {
    after("/*", handler);
    return this;
  }

  // ********************************************************************************************
  // WebSocket
  // ********************************************************************************************
//
//    /**
//     * Adds a WebSocket handler on the specified path.
//     * The method can only be called inside a {@link Javalin#routes(EndpointGroup)}.
//     *
//     * @see <a href="https://javalin.io/documentation#websockets">WebSockets in docs</a>
//     */
//    public static void ws(String path, Consumer<WsHandler> ws) {
//        me().ws(prefixPath(path), ws);
//    }
//
//    /**
//     * Adds a WebSocket handler with the given roles for the specified path.
//     * The method can only be called inside a {@link Javalin#routes(EndpointGroup)}.
//     *
//     * @see <a href="https://javalin.io/documentation#websockets">WebSockets in docs</a>
//     */
//    public static void ws(String path, Consumer<WsHandler> ws, Set<Role> permittedRoles) {
//        me().ws(prefixPath(path), ws, permittedRoles);
//    }
//
//    /**
//     * Adds a WebSocket handler on the current path.
//     * The method can only be called inside a {@link Javalin#routes(EndpointGroup)}.
//     *
//     * @see <a href="https://javalin.io/documentation#websockets">WebSockets in docs</a>
//     */
//    public static void ws(Consumer<WsHandler> ws) {
//        me().ws(prefixPath(""), ws);
//    }
//
//    /**
//     * Adds a WebSocket handler with the given roles for the current path.
//     * The method can only be called inside a {@link Javalin#routes(EndpointGroup)}.
//     *
//     * @see <a href="https://javalin.io/documentation#websockets">WebSockets in docs</a>
//     */
//    public static void ws(Consumer<WsHandler> ws, Set<Role> permittedRoles) {
//        me().ws(prefixPath(""), ws, permittedRoles);
//    }
//
//    /**
//     * Adds a WebSocket before handler for the specified path to the {@link Javalin} instance.
//     * The method can only be called inside a {@link Javalin#routes(EndpointGroup)}.
//     */
//    public Javalin wsBefore(String path, Consumer<WsHandler> wsHandler) {
//        return me().wsBefore(prefixPath(path), wsHandler);
//    }
//
//    /**
//     * Adds a WebSocket before handler for the current path to the {@link Javalin} instance.
//     * The method can only be called inside a {@link Javalin#routes(EndpointGroup)}.
//     */
//    public Javalin wsBefore(Consumer<WsHandler> wsHandler) {
//        return me().wsBefore(prefixPath("/*"), wsHandler);
//    }
//
//    /**
//     * Adds a WebSocket after handler for the specified path to the {@link Javalin} instance.
//     * The method can only be called inside a {@link Javalin#routes(EndpointGroup)}.
//     */
//    public Javalin wsAfter(String path, Consumer<WsHandler> wsHandler) {
//        return me().wsAfter(prefixPath(path), wsHandler);
//    }
//
//    /**
//     * Adds a WebSocket after handler for the current path to the {@link Javalin} instance.
//     * The method can only be called inside a {@link Javalin#routes(EndpointGroup)}.
//     */
//    public Javalin wsAfter(Consumer<WsHandler> wsHandler) {
//        return me().wsAfter(prefixPath("/*"), wsHandler);
//    }
//
//    // ********************************************************************************************
//    // Server-sent events
//    // ********************************************************************************************
//
//    public static void sse(String path, Consumer<SseClient> client) {
//        me().sse(prefixPath(path), client);
//    }
//
//    public static void sse(String path, Consumer<SseClient> client, Set<Role> permittedRoles) {
//        me().sse(prefixPath(path), client, permittedRoles);
//    }
//
//    public static void sse(Consumer<SseClient> client) {
//        me().sse(prefixPath(""), client);
//    }
//
//    public static void sse(Consumer<SseClient> client, Set<Role> permittedRoles) {
//        me().sse(prefixPath(""), client, permittedRoles);
//    }

  static class Entry implements Routing.Entry {

    private final Type type;
    private final String path;
    private final Handler handler;
    private final Set<Role> roles;

    Entry(Type type, String path, Handler handler, Set<Role> roles) {
      this.type = type;
      this.path = path;
      this.handler = handler;
      this.roles = roles;
    }

    Entry(String path, Handler handler) {
      this.path = path;
      this.handler = handler;
      this.type = null;
      this.roles = null;
    }

    @Override
    public Type getType() {
      return type;
    }

    @Override
    public String getPath() {
      return path;
    }

    @Override
    public Handler getHandler() {
      return handler;
    }

    @Override
    public Set<Role> getRoles() {
      return roles;
    }
  }
}
