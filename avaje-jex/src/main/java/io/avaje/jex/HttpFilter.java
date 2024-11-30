package io.avaje.jex;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

/**
 * A filter used to pre/post-process incoming requests. Pre-processing occurs before the
 * application's exchange handler is invoked, and post-processing occurs after the exchange handler
 * returns. Filters are organized in chains, and are associated with {@link Context} instances.
 *
 * <p>Each {@code HttpFilter} in the chain, invokes the next filter within its own {@link
 * #filter(Context, FilterChain)} implementation. The final {@code HttpFilter} in the chain invokes
 * the applications exchange handler.
 */
@FunctionalInterface
public interface HttpFilter {

  /**
   * Asks this filter to pre/post-process the given request. The filter can:
   *
   * <ul>
   *   <li>Examine or modify the request headers.
   *   <li>Set attribute objects in the context, which other filters or the handler can access.
   *   <li>Decide to either:
   *       <ol>
   *         <li>Invoke the next filter in the chain, by calling {@link FilterChain#proceed}.
   *         <li>Terminate the chain of invocation, by <b>not</b> calling {@link
   *             FilterChain#proceed()}.
   *       </ol>
   *   <li>If option 1. above is taken, then when filter() returns all subsequent filters in the
   *       Chain have been called, and the response headers can be examined or modified.
   *   <li>If option 2. above is taken, then this Filter must use the Context to send back an
   *       appropriate response.
   * </ul>
   *
   * @param ctx the {@code Context} of the current request
   * @param chain the {@code FilterChain} which allows the next filter to be invoked
   */
  void filter(Context ctx, FilterChain chain) throws IOException;

  /**
   * Filter chain that contains all subsequent filters that are configured, as well as the final
   * route.
   */
  @FunctionalInterface
  interface FilterChain {

    /**
     * Calls the next filter in the chain, or else the user's exchange handler, if this is the final
     * filter in the chain. The {@link HttpFilter} may decide to terminate the chain, by not calling
     * this method. In this case, the filter <b>must</b> send the response to the request, because
     * the application's {@linkplain HttpExchange exchange} handler will not be invoked.
     *
     * @throws IOException if an I/O error occurs
     */
    void proceed() throws IOException;
  }
}
