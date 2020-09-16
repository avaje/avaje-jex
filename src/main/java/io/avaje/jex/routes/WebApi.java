package io.avaje.jex.routes;

import io.avaje.jex.Handler;
import io.avaje.jex.Role;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class WebApi {

  private static final WebApi me = new WebApi();

  private final List<WebApiEntry> handlers = new ArrayList<>();
  private final List<WebApiEntry> before = new ArrayList<>();
  private final List<WebApiEntry> after = new ArrayList<>();

  private final Deque<String> pathDeque = new ArrayDeque<>();

  private WebApi() {
    // hide
  }

  private static WebApi me() {
    return me;
  }

  public static WebApiEntries all() {
    return me().allEntries();
  }

  private WebApiEntries allEntries() {
    return new WebApiEntries(handlers, before, after);
  }


  private String path(String path) {
    return String.join("", pathDeque) + ((path.startsWith("/") || path.isEmpty()) ? path : "/" + path);
  }

  private void addEndpoints(String path, EndpointGroup endpointGroup) {
    path = path.startsWith("/") ? path : "/" + path;
    pathDeque.addLast(path);
    endpointGroup.addEndpoints();
    pathDeque.removeLast();
  }

  public static void path(String path, EndpointGroup endpointGroup) {
    me().addEndpoints(path, endpointGroup);
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


  private void add(HandlerType verb, String path, Handler handler, Set<Role> roles) {
    handlers.add(new WebApiEntry(verb, path(path), handler, roles));
  }

  private void addBefore(String path, Handler handler) {
    before.add(new WebApiEntry(path(path), handler));
  }

  private void addAfter(String path, Handler handler) {
    after.add(new WebApiEntry(path(path), handler));
  }

  // ********************************************************************************************
  // HTTP verbs
  // ********************************************************************************************

  public static void get(String path, Handler handler, Set<Role> permittedRoles) {
    me().add(HandlerType.GET, path, handler, permittedRoles);
  }

  public static void get(String path, Handler handler) {
    get(path, handler, Collections.emptySet());
  }

  public static void get(Handler handler) {
    get("", handler);
  }

  public static void get(Handler handler, Set<Role> permittedRoles) {
    get("", handler, permittedRoles);
  }

  public static void post(String path, Handler handler, Set<Role> permittedRoles) {
    me().add(HandlerType.POST, path, handler, permittedRoles);
  }

  public static void post(String path, Handler handler) {
    post(path, handler, Collections.emptySet());
  }

  public static void post(Handler handler) {
    post("", handler);
  }

  public static void post(Handler handler, Set<Role> permittedRoles) {
    post("", handler, permittedRoles);
  }

  // ********************************************************************************************
  // Before/after handlers (filters)
  // ********************************************************************************************

  public static void before(String path, Handler handler) {
    me().addBefore(path, handler);
  }

  public static void before(Handler handler) {
    before("/*", handler);
  }

  public static void after(String path, Handler handler) {
    me().addAfter(path, handler);
  }

  public static void after(Handler handler) {
    after("/*", handler);
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

}
