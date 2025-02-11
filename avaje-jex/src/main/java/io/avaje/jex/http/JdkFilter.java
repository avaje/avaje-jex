package io.avaje.jex.http;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Filter.Chain;

final class JdkFilter implements HttpFilter {

  private final Filter delegate;

  JdkFilter(Filter delegate) {
    this.delegate = delegate;
  }

  @Override
  public void filter(Context ctx, FilterChain chain) {
    try {
      delegate.doFilter(ctx.exchange(), new Chain(List.of(), ex -> chain.proceed()));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
