package io.avaje.jex.http;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Filter.Chain;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

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
