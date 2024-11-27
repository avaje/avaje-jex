package io.avaje.jex;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

/** Filter chain that contains all subsequent filters that are configured, as well as the final route. */
@FunctionalInterface
public interface FilterChain {

  /**
   * Calls the next filter in the chain, or else the user's exchange handler, if this is the final
   * filter in the chain. The {@link HttpFilter} may decide to terminate the chain, by not calling
   * this method. In this case, the filter <b>must</b> send the response to the request, because the
   * application's {@linkplain HttpExchange exchange} handler will not be invoked.
   *
   * @throws IOException if an I/O error occurs
   */
  void proceed() throws IOException;
}
