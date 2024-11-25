package io.avaje.jex;

import java.io.IOException;

/**
 * A handler which is invoked to process HTTP exchanges. Each HTTP exchange is handled by one of
 * these handlers.
 */
@FunctionalInterface
public interface ExchangeHandler {

  /**
   * Handle the given request and generate an appropriate response. See {@link Context} for a
   * description of the steps involved in handling an exchange.
   *
   * @param ctx the request context containing the request from the client and used to send the
   *     response
   */
  void handle(Context ctx) throws IOException;
}
