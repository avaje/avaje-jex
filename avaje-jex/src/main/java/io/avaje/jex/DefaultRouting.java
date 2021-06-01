package io.avaje.jex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 *
 */
class DefaultRouting implements Routing {

  private final List<MutableEntry> handlers = new ArrayList<>();
  private final Deque<String> pathDeque = new ArrayDeque<>();

  /**
   * Last entry that we can add permitted roles to.
   */
  private MutableEntry lastEntry;

  DefaultRouting() {
    // hide
  }

  @Override
  public List<Routing.Entry> all() {
    return handlers.stream()
      .map(MutableEntry::toEntry)
      .collect(toList());
  }

  private String path(String path) {
    return String.join("", pathDeque) + ((path.startsWith("/") || path.isEmpty()) ? path : "/" + path);
  }

  private void addEndpoints(String path, Group group) {
    path = path.startsWith("/") ? path : "/" + path;
    pathDeque.addLast(path);
    group.addGroup();
    pathDeque.removeLast();
  }

  @Override
  public Routing add(Routing.Service routes) {
    routes.add(this);
    return this;
  }

  @Override
  public Routing addAll(Collection<Routing.Service> routes) {
    for (Service route : routes) {
      route.add(this);
    }
    return this;
  }

  @Override
  public Routing path(String path, Group group) {
    addEndpoints(path, group);
    return this;
  }

  @Override
  public Routing withRoles(Set<Role> permittedRoles) {
    lastEntry.withRoles(permittedRoles);
    return this;
  }


  private void add(Type verb, String path, Handler handler) {
    lastEntry = new MutableEntry(verb, path(path), handler);
    handlers.add(lastEntry);
  }

  private void addBefore(String path, Handler handler) {
    add(Type.BEFORE, path(path), handler);
  }

  private void addAfter(String path, Handler handler) {
    add(Type.AFTER, path(path), handler);
  }

  // ********************************************************************************************
  // HTTP verbs
  // ********************************************************************************************

  @Override
  public Routing get(String path, Handler handler) {
    add(Type.GET, path, handler);
    return this;
  }

  @Override
  public Routing get(Handler handler) {
    get("", handler);
    return this;
  }

  @Override
  public Routing post(String path, Handler handler) {
    add(Type.POST, path, handler);
    return this;
  }

  @Override
  public Routing post(Handler handler) {
    post("", handler);
    return this;
  }

  @Override
  public Routing put(String path, Handler handler) {
    add(Type.PUT, path, handler);
    return this;
  }

  @Override
  public Routing put(Handler handler) {
    put("", handler);
    return this;
  }

  @Override
  public Routing patch(String path, Handler handler) {
    add(Type.PATCH, path, handler);
    return this;
  }

  @Override
  public Routing patch(Handler handler) {
    patch("", handler);
    return this;
  }

  @Override
  public Routing delete(String path, Handler handler) {
    add(Type.DELETE, path, handler);
    return this;
  }

  @Override
  public Routing delete(Handler handler) {
    delete("", handler);
    return this;
  }

  @Override
  public Routing head(String path, Handler handler) {
    add(Type.HEAD, path, handler);
    return this;
  }

  @Override
  public Routing head(Handler handler) {
    head("", handler);
    return this;
  }

  @Override
  public Routing trace(String path, Handler handler) {
    add(Type.TRACE, path, handler);
    return this;
  }

  @Override
  public Routing trace(Handler handler) {
    trace("", handler);
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

  private static class MutableEntry {

    private final Type type;
    private final String path;
    private final Handler handler;
    private Set<Role> roles;

    MutableEntry(Type type, String path, Handler handler) {
      this.type = type;
      this.path = path;
      this.handler = handler;
      this.roles = Collections.emptySet();
    }

    void withRoles(Set<Role> roles) {
      this.roles = roles;
    }

    Routing.Entry toEntry() {
      return new Entry(type, path, handler, roles);
    }
  }

  private static class Entry implements Routing.Entry {

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
