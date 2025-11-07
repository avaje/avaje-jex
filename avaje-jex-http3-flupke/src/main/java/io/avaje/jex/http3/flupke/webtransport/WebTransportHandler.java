package io.avaje.jex.http3.flupke.webtransport;

import java.util.Objects;
import java.util.function.Consumer;

import io.avaje.jex.http3.flupke.webtransport.WebTransportEvent.BiStream;
import io.avaje.jex.http3.flupke.webtransport.WebTransportEvent.UniStream;
import io.avaje.jex.http3.flupke.webtransport.WebTransportEvent.Close;
import io.avaje.jex.http3.flupke.webtransport.WebTransportEvent.Open;

public interface WebTransportHandler {

  static Builder builder() {
    return new Builder();
  }

  /**
   * Handles a new WebTransport session opening.
   *
   * @param context The Open context.
   */
  void onOpen(Open context);

  /**
   * Handles a WebTransport session closing.
   *
   * @param context The Close context, which includes the closing code and message.
   */
  void onClose(Close context);

  /**
   * Handles a message context, which involves receiving a data frame on a wtStream.
   *
   * @param context The BiStream context, which includes the WebTransportStream.
   */
  void onUniDirectionalStream(UniStream context);

  /**
   * Handles a message context, which involves receiving a data frame on a wtStream.
   *
   * @param context The BiStream context, which includes the WebTransportStream.
   */
  void onBiDirectionalStream(BiStream context);

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

    /** Factory method to start the building process. */
    public static Builder builder() {
      return new Builder();
    }

    // --- Fluent Setter Methods ---

    public Builder onOpen(Consumer<Open> handler) {
      this.openHandler = Objects.requireNonNull(handler);
      return this;
    }

    public Builder onClose(Consumer<Close> handler) {
      this.closeHandler = Objects.requireNonNull(handler);
      return this;
    }

    public Builder onUniDirectionalStream(Consumer<UniStream> handler) {
      this.unidirectional = Objects.requireNonNull(handler);
      return this;
    }

    public Builder onBiDirectionalStream(Consumer<BiStream> handler) {
      this.bidirectional = Objects.requireNonNull(handler);
      return this;
    }

    /**
     * Finishes the configuration and returns the fully built WtContextHandler.
     *
     * @return A WtContextHandler that delegates to the configured Consumer functions.
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
