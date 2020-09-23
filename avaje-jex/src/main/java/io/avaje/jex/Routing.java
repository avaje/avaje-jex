package io.avaje.jex;

import java.util.List;
import java.util.Set;

public interface Routing {

  /**
   * Add a group of route handlers with a common path prefix.
   */
  Routing path(String path, Group group);

  /**
   * Add a GET handler with roles.
   */
  Routing get(String path, Handler handler, Set<Role> permittedRoles);

  /**
   * Add a GET handler.
   */
  Routing get(String path, Handler handler);

  /**
   * Add a GET handler for "/".
   */
  Routing get(Handler handler);

  /**
   * Add a GET handler for "/" with roles.
   */
  Routing get(Handler handler, Set<Role> permittedRoles);

  /**
   * Add a POST handler with roles.
   */
  Routing post(String path, Handler handler, Set<Role> permittedRoles);

  /**
   * Add a POST handler with roles.
   */
  Routing post(String path, Handler handler);

  /**
   * Add a POST handler for "/".
   */
  Routing post(Handler handler);

  /**
   * Add a POST handler for "/" with roles.
   */
  Routing post(Handler handler, Set<Role> permittedRoles);

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
    TRACE;//, CONNECT, OPTIONS, INVALID;
  }
}
