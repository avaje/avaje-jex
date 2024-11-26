package io.avaje.jex.jdk;

import java.io.IOException;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import io.avaje.jex.HttpFilter;

public class JdkFilter extends Filter {

  private final HttpFilter handler;

  public JdkFilter(HttpFilter handler) {
    this.handler = handler;
  }

  @Override
  public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
    var ctx = (JdkContext) exchange.getAttribute("JdkContext");
    handler.filter(ctx, () -> chain.doFilter(exchange));
  }

  @Override
  public String description() {
    return "JexFilter";
  }
}
