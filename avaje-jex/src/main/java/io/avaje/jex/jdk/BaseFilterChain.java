package io.avaje.jex.jdk;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import io.avaje.jex.ExchangeHandler;
import io.avaje.jex.HttpFilter;
import io.avaje.jex.HttpFilter.FilterChain;

final class BaseFilterChain implements FilterChain {

  private final ListIterator<HttpFilter> iter;
  private final ExchangeHandler handler;
  private final JdkContext ctx;

  BaseFilterChain(List<HttpFilter> filters, ExchangeHandler handler, JdkContext ctx) {
    this.iter = filters.listIterator();
    this.handler = handler;
    this.ctx = ctx;
  }

  @Override
  public void proceed() throws IOException {
    if (!iter.hasNext()) {
      handler.handle(ctx);
      ctx.setMode(Mode.AFTER);
    } else {
      iter.next().filter(ctx, this);
    }
  }
}