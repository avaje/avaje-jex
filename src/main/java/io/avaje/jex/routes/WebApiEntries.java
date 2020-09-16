package io.avaje.jex.routes;

import java.util.List;

class WebApiEntries {

  private final List<WebApiEntry> handlers;
  private final List<WebApiEntry> before;
  private final List<WebApiEntry> after;

  WebApiEntries(List<WebApiEntry> handlers, List<WebApiEntry> before, List<WebApiEntry> after) {
    this.handlers = handlers;
    this.before = before;
    this.after = after;
  }

  List<WebApiEntry> getHandlers() {
    return handlers;
  }

  List<WebApiEntry> getBefore() {
    return before;
  }

  List<WebApiEntry> getAfter() {
    return after;
  }

  /**
   * Clearing all the registered handlers and filters such that we can
   * create another server (e.g. testing).
   */
  void clear() {
    handlers.clear();
    before.clear();
    after.clear();
  }
}
