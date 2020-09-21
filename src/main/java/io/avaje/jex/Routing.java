package io.avaje.jex;

import java.util.List;
import java.util.Set;

public interface Routing {

  Routing path(String path, Group group);

  Routing get(String path, Handler handler, Set<Role> permittedRoles);

  Routing get(String path, Handler handler);

  Routing get(Handler handler);

  Routing get(Handler handler, Set<Role> permittedRoles);

  Routing post(String path, Handler handler, Set<Role> permittedRoles);

  Routing post(String path, Handler handler);

  Routing post(Handler handler);

  Routing post(Handler handler, Set<Role> permittedRoles);

  Routing before(String path, Handler handler);

  Routing before(Handler handler);

  Routing after(String path, Handler handler);

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
     * @param routing The routing to add to.
     */
    void add(Routing routing);
  }

  interface Entry {

    Type getType();

    String getPath();

    Handler getHandler();

    Set<Role> getRoles();
  }

  enum Type {
    BEFORE, AFTER, GET, POST, PUT, PATCH, DELETE, HEAD, TRACE;//, CONNECT, OPTIONS, INVALID;
  }
}
