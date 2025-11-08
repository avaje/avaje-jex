package io.avaje.jex.http3.flupke.webtransport;

import java.util.Objects;
import java.util.function.Consumer;

import io.avaje.jex.http3.flupke.webtransport.WebTransportEvent.BiStream;
import io.avaje.jex.http3.flupke.webtransport.WebTransportEvent.Close;
import io.avaje.jex.http3.flupke.webtransport.WebTransportEvent.Open;
import io.avaje.jex.http3.flupke.webtransport.WebTransportEvent.UniStream;

/**
 * Defines the contract for handling lifecycle events within a single WebTransport session.
 *
 * <p>This interface is typically implemented using the nested {@link Builder} to create a handler
 * that delegates each event type to a custom {@code Consumer} function.
 */
public interface WebTransportHandler {

  /**
   * Returns a new {@code Builder} instance for fluently creating a {@code WebTransportHandler}.
   *
   * @return A new Builder.
   */
  static Builder builder() {
    return new Builder();
  }

  /**
   * Handles a new WebTransport session opening event.
   *
   * @param context The {@code Open} context containing session details.
   */
  void onOpen(Open context);

  /**
   * Handles a WebTransport session closing event.
   *
   * @param context The {@code Close} context, which includes the closing code and message.
   */
  void onClose(Close context);

  /**
   * Handles a new unidirectional stream opened by the client to the server.
   *
   * <p>The handler should consume the data from the stream via {@link UniStream#requestStream()}.
   *
   * @param context The {@code UniStream} context, which provides access to the read-only {@code
   *     InputStream}.
   */
  void onUniDirectionalStream(UniStream context);

  /**
   * Handles a new bidirectional stream opened by the client.
   *
   * <p>The handler can read data from and write data to the stream via {@link
   * BiStream#requestStream()}.
   *
   * @param context The {@code BiStream} context, which provides access to the request/response
   *     streams.
   */
  void onBiDirectionalStream(BiStream context);

  /**
   * A fluent builder for creating a {@link WebTransportHandler} implementation.
   *
   * <p>The builder allows setting a {@code Consumer} for each specific WebTransport event. Events
   * not configured will use a default implementation: {@code onOpen} and {@code onClose} default to
   * a no-op, while stream handlers default to throwing an {@code UnsupportedOperationException}.
   */
  final class Builder {

    // Consumers to hold the logic for each event type.
    // Defaults to an empty operation (no-op) if not explicitly set.
    private Consumer<Open> openHandler = ctx -> {};
    private Consumer<Close> closeHandler = ctx -> {};
    private Consumer<BiStream> bidirectional =
        ctx -> {
          throwUOE("bidirectional handler not implemented");
        };

    private Consumer<UniStream> unidirectional =
        ctx -> {
          throwUOE("unidirectional handler not implemented");
        };

    private Builder() {}

    private void throwUOE(String message) {
      throw new UnsupportedOperationException(message);
    }

    /**
     * Factory method to start the building process.
     *
     * @return A new instance of the Builder.
     */
    public static Builder builder() {
      return new Builder();
    }

    // --- Fluent Setter Methods ---

    /**
     * Sets the consumer function to be executed when a session is opened.
     *
     * @param handler The consumer to handle the {@code Open} event context. Must not be null.
     * @return This builder instance for chaining.
     */
    public Builder onOpen(Consumer<Open> handler) {
      this.openHandler = Objects.requireNonNull(handler);
      return this;
    }

    /**
     * Sets the consumer function to be executed when a session is closed.
     *
     * @param handler The consumer to handle the {@code Close} event context. Must not be null.
     * @return This builder instance for chaining.
     */
    public Builder onClose(Consumer<Close> handler) {
      this.closeHandler = Objects.requireNonNull(handler);
      return this;
    }

    /**
     * Sets the consumer function to be executed when the client opens a new unidirectional stream.
     *
     * @param handler The consumer to handle the {@code UniStream} event context. Must not be null.
     * @return This builder instance for chaining.
     */
    public Builder onUniDirectionalStream(Consumer<UniStream> handler) {
      this.unidirectional = Objects.requireNonNull(handler);
      return this;
    }

    /**
     * Sets the consumer function to be executed when the client opens a new bidirectional stream.
     *
     * @param handler The consumer to handle the {@code BiStream} event context. Must not be null.
     * @return This builder instance for chaining.
     */
    public Builder onBiDirectionalStream(Consumer<BiStream> handler) {
      this.bidirectional = Objects.requireNonNull(handler);
      return this;
    }

    /**
     * Finishes the configuration and returns the fully built {@code WebTransportHandler}.
     *
     * @return A {@code WebTransportHandler} that delegates to the configured Consumer functions.
     */
    public WebTransportHandler build() {
      return new WebTransportHandler() {
        @Override
        public void onOpen(Open context) {
          openHandler.accept(context);
        }

        @Override
        public void onClose(Close context) {
          closeHandler.accept(context);
        }

        @Override
        public void onUniDirectionalStream(UniStream context) {
          unidirectional.accept(context);
        }

        @Override
        public void onBiDirectionalStream(BiStream context) {
          bidirectional.accept(context);
        }
      };
    }
  }
}
