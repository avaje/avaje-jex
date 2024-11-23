package io.avaje.jex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import io.avaje.jex.jdk.JdkFilter;

class DefaultRouting implements Routing {

  private final List<Routing.Entry> handlers = new ArrayList<>();
  private final List<HttpFilter> filters = new ArrayList<>();
  private final Deque<String> pathDeque = new ArrayDeque<>();

  /**
   * Last entry that we can add permitted roles to.
   */
  private Entry lastEntry;

  @Override
  public List<Routing.Entry> handlers() {
    return handlers;
  }

  @Override
  public List<HttpFilter> filters() {
    return filters;
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
    if (lastEntry == null) {
      throw new IllegalStateException("Must call withRoles() after adding a route");
    }
    lastEntry.withRoles(permittedRoles);
    return this;
  }

  @Override
  public Routing withRoles(Role... permittedRoles) {
    return withRoles(Set.of(permittedRoles));
  }

  private void add(Type verb, String path, Handler handler) {
    lastEntry = new Entry(verb, path(path), handler);
    handlers.add(lastEntry);
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
  // Filters
  // ********************************************************************************************

  @Override
  public Routing filter(HttpFilter handler) {
    filters.add(handler);
    return this;
  }


  private static class Entry implements Routing.Entry {

    private final Type type;
    private final String path;
    private final Handler handler;
    private Set<Role> roles = Collections.emptySet();

    Entry(Type type, String path, Handler handler) {
      this.type = type;
      this.path = path;
      this.handler = handler;
    }

    void withRoles(Set<Role> roles) {
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
