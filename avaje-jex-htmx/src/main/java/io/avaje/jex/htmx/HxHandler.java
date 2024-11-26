package io.avaje.jex.htmx;

import io.avaje.jex.ExchangeHandler;

/**
 * Wrap a Handler with filtering for Htmx specific headers.
 * <p>
 * The underlying Handler will not be invoked unless the request
 * is a Htmx request and matches the required attributes.
 */
public interface HxHandler {

  /**
   * Create a builder that wraps the underlying handler with Htmx
   * specific attribute matching.
   */
  static Builder builder(ExchangeHandler delegate) {
    return new DHxHandlerBuilder(delegate);
  }

  /**
   * Build the Htmx request handler.
   */
  interface Builder {

    /**
     * Match on the given target.
     */
    Builder target(String target);

    /**
     * Match on the given trigger.
     */
    Builder trigger(String trigger);

    /**
     * Match on the given trigger name.
     */
    Builder triggerName(String triggerName);

    /**
     * Build and return the Handler.
     */
    ExchangeHandler build();
  }
}
