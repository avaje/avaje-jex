package io.avaje.jex;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface Routing {

  /**
   * Add the routes provided by the Routing Service.
   */
  Routing add(Routing.Service routes);

  /**
   * Add all the routes provided by the Routing Services.
   */
  Routing addAll(Collection<Routing.Service> routes);

  /**
   * Specify permittedRoles for the last added handler.
   * <pre>{@code
   *
   *  routing
   *  .get("/customers", getHandler).withRoles(readRoles)
   *  .post("/customers", postHandler).withRoles(writeRoles)
   *  ...
   *
   * }</pre>
   *
   * @param permittedRoles The permitted roles required for the last handler
   */
  Routing withRoles(Set<Role> permittedRoles);

  /**
   * Add a group of route handlers with a common path prefix.
   */
  Routing path(String path, Group group);

  /**
   * Add a HEAD handler.
   */
  Routing head(String path, Handler handler);

  /**
   * Add a HEAD handler for "/".
   */
  Routing head(Handler handler);

  /**
   * Add a GET handler.
   */
  Routing get(String path, Handler handler);

  /**
   * Add a GET handler for "/".
   */
  Routing get(Handler handler);

  /**
   * Add a POST handler.
   */
  Routing post(String path, Handler handler);

  /**
   * Add a POST handler for "/".
   */
  Routing post(Handler handler);

  /**
   * Add a PUT handler.
   */
  Routing put(String path, Handler handler);

  /**
   * Add a PUT handler for "/".
   */
  Routing put(Handler handler);

  /**
   * Add a PATCH handler.
   */
  Routing patch(String path, Handler handler);

  /**
   * Add a PATCH handler for "/".
   */
  Routing patch(Handler handler);

  /**
   * Add a DELETE handler.
   */
  Routing delete(String path, Handler handler);

  /**
   * Add a DELETE handler for "/".
   */
  Routing delete(Handler handler);

  /**
   * Add a TRACE handler.
   */
  Routing trace(String path, Handler handler);

  /**
   * Add a TRACE handler for "/".
   */
  Routing trace(Handler handler);

  /**
   * Add a before filter for the given path.
   */
  Routing before(String path, Handler handler);

  /**
   * Add a before filter for all requests.
   */
  Routing before(Handler handler);

  /**
   * Add a after filter for the given path.
   */
  Routing after(String path, Handler handler);

  /**
   * Add an after filter for all requests.
   */
  Routing after(Handler handler);

  /**
   * Return all the registered handlers.
   */
  List<Entry> all();

  /**
   * A group of routing entries prefixed by a common path.
   */
  @FunctionalInterface
  interface Group {

    /**
     * Add the group of entries with a common prefix.
     */
    void addGroup();
  }

  /**
   * Adds to the Routing.
   */
  @FunctionalInterface
  interface Service {

    /**
     * Add to the routing.
     *
     * @param routing The routing to add handlers to.
     */
    void add(Routing routing);
  }

  /**
   * A routing entry.
   */
  interface Entry {

    /**
     * Return the type of entry.
     */
    Type getType();

    /**
     * Return the full path of the entry.
     */
    String getPath();

    /**
     * Return the handler.
     */
    Handler getHandler();

    /**
     * Return the roles.
     */
    Set<Role> getRoles();
  }

  /**
   * The type of route entry.
   */
  enum Type {
    /**
     * Before filter.
     */
    BEFORE,
    /**
     * After filter.
     */
    AFTER,
    /**
     * Http GET.
     */
    GET,
    /**
     * Http POST.
     */
    POST,
    /**
     * HTTP PUT.
     */
    PUT,
    /**
     * HTTP PATCH.
     */
    PATCH,
    /**
     * HTTP DELETE.
     */
    DELETE,
    /**
     * HTTP HEAD.
     */
    HEAD,
    /**
     * HTTP TRACE.
     */
    TRACE//, CONNECT, OPTIONS, INVALID;
  }
}
