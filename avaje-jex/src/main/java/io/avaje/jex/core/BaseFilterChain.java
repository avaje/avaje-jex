package io.avaje.jex.core;

import java.util.Iterator;
import java.util.List;

import io.avaje.jex.ExchangeHandler;
import io.avaje.jex.HttpFilter;
import io.avaje.jex.HttpFilter.FilterChain;

final class BaseFilterChain implements FilterChain {

  private final Iterator<HttpFilter> iter;
  private final ExchangeHandler handler;
  private final JdkContext ctx;
  private final ServiceManager mgr;

  BaseFilterChain(
      List<HttpFilter> filters, ExchangeHandler handler, JdkContext ctx, ServiceManager mgr) {
    this.iter = filters.iterator();
    this.handler = handler;
    this.ctx = ctx;
    this.mgr = mgr;
  }

  @Override
  public void proceed() {

    if (iter.hasNext()) {
      iter.next().filter(ctx, this);
    } else {
      try {
        if (!ctx.responseSent()) {
          handler.handle(ctx);
        }
      } catch (Exception t) {
        mgr.handleException(ctx, t);
      }
    }
    ctx.setMode(Mode.AFTER);
  }
}
