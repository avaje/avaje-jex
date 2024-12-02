package io.avaje.jex;

import java.io.IOException;

/**
 * A functional interface representing an HTTP request handler.
 *
 * <p>Implementations of this interface are responsible for processing incoming HTTP requests and
 * generating appropriate responses. The {@code handle} method provides access to a {@link Context}
 * object, which encapsulates the request and response details.
 *
 * @see Context
 */
@FunctionalInterface
public interface ExchangeHandler {

  /**
   * Handles the given HTTP request and generates a response.
   *
   * <p>The {@link Context} object provides access to request information such as headers,
   * parameters, and body, as well as methods for constructing and sending the response.
   *
   * @param ctx The context object containing the request and response details.
   * @throws IOException if an I/O error occurs during request processing or response generation.
   */
  void handle(Context ctx) throws Exception;
}
