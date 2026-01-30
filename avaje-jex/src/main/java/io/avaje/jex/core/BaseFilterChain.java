package io.avaje.jex.core;

import java.util.Iterator;

import io.avaje.jex.http.ExchangeHandler;
import io.avaje.jex.http.HttpFilter;
import io.avaje.jex.http.HttpFilter.FilterChain;

final class BaseFilterChain implements FilterChain {

  private final Iterator<HttpFilter> filters;
  private final ExchangeHandler handler;
  private final JdkContext ctx;
  private final ServiceManager mgr;

  BaseFilterChain(Iterator<HttpFilter> filters, ExchangeHandler handler, JdkContext ctx, ServiceManager mgr) {
    this.filters = filters;
    this.handler = handler;
    this.ctx = ctx;
    this.mgr = mgr;
  }

  @Override
  public void proceed() {
    if (filters.hasNext()) {
      filters.next().filter(ctx, this);
    } else {
      try {
        if (!ctx.responseSent()) {
          ctx.setMode(Mode.EXCHANGE);
          handler.handle(ctx);
        }
      } catch (Exception t) {
        mgr.handleException(ctx, t);
      }
    }
    ctx.setMode(Mode.AFTER);
  }
}
