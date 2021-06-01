package io.avaje.jex.routes;

import io.avaje.jex.*;
import io.avaje.jex.spi.SpiRoutes;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

public class RoutesBuilder {

  private final EnumMap<Routing.Type, RouteIndex> typeMap = new EnumMap<>(Routing.Type.class);
  private final List<SpiRoutes.Entry> before = new ArrayList<>();
  private final List<SpiRoutes.Entry> after = new ArrayList<>();
  private final boolean ignoreTrailingSlashes;
  private final AccessManager accessManager;

  public RoutesBuilder(Routing routing, Jex jex) {
    this.accessManager = jex.inner.accessManager;
    this.ignoreTrailingSlashes = jex.inner.ignoreTrailingSlashes;
    for (Routing.Entry handler : routing.all()) {
      switch (handler.getType()) {
        case BEFORE:
          before.add(filter(handler));
          break;
        case AFTER:
          after.add(filter(handler));
          break;
        default:
          typeMap.computeIfAbsent(handler.getType(), h -> new RouteIndex()).add(convert(handler));
      }
    }
  }

  private FilterEntry filter(Routing.Entry entry) {
    return new FilterEntry(entry, ignoreTrailingSlashes);
  }

  private SpiRoutes.Entry convert(Routing.Entry handler) {
    final PathParser pathParser = new PathParser(handler.getPath(), ignoreTrailingSlashes);
    return new RouteEntry(pathParser, extractHandler(handler));
  }

  private Handler extractHandler(Routing.Entry entry) {
    if (entry.getRoles().isEmpty() || accessManager == null) {
      return entry.getHandler();
    }
    return new AccessHandler(accessManager, entry.getHandler(), entry.getRoles());
  }

  public SpiRoutes build() {
    return new Routes(typeMap, before, after);
  }

  /**
   * Wrap the handler with access check based on permitted roles.
   */
  static class AccessHandler implements Handler {

    private final AccessManager manager;
    private final Handler handler;
    private final Set<Role> roles;

    AccessHandler(AccessManager manager, Handler handler, Set<Role> roles) {
      this.manager = manager;
      this.handler = handler;
      this.roles = roles;
    }

    @Override
    public void handle(Context ctx) {
      manager.manage(handler, ctx, roles);
    }
  }
}
