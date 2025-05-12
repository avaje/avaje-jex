package io.avaje.jex.compression;

import io.avaje.jex.core.Constants;
import io.avaje.jex.http.Context;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * OutputStream implementation that conditionally compresses the output based on configuration and
 * request headers.
 */
public final class CompressedOutputStream extends OutputStream {

  private final int minSizeForCompression;
  private final CompressionConfig compression;
  private final Context ctx;
  private final OutputStream originStream;

  private OutputStream compressedStream;
  private boolean compressionDecided;

  public CompressedOutputStream(
      CompressionConfig compression, Context ctx, OutputStream originStream) {
    this.minSizeForCompression = compression.minSizeForCompression();
    this.compression = compression;
    this.ctx = ctx;
    this.originStream = originStream;
  }

  private void decideCompression(int length) throws IOException {
    if (!compressionDecided) {
      boolean compressionAllowed =
          compressedStream == null
              && ctx.responseHeader(Constants.CONTENT_RANGE) == null
              && compression.allowsForCompression(ctx.responseHeader(Constants.CONTENT_TYPE));

      if (compressionAllowed && length >= minSizeForCompression) {
        Optional<Compressor> compressor;
        compressor = findMatchingCompressor(ctx.headerValues(Constants.ACCEPT_ENCODING));
        if (compressor.isPresent()) {
          this.compressedStream = compressor.get().compress(originStream);
          ctx.header(Constants.CONTENT_ENCODING, compressor.get().encoding());
        }
      }
      compressionDecided = true;
    }
  }

  @Override
  public void write(byte[] bytes, int offset, int length) throws IOException {
    decideCompression(length);
    (compressedStream != null ? compressedStream : originStream).write(bytes, offset, length);
  }

  @Override
  public void write(int byteVal) throws IOException {
    decideCompression(1);
    (compressedStream != null ? compressedStream : originStream).write(byteVal);
  }

  @Override
  public void close() throws IOException {
    if (compressedStream != null) {
      compressedStream.close();
    } else {
      originStream.close();
    }
  }

  private Optional<Compressor> findMatchingCompressor(List<String> acceptedEncoding) {
    if (acceptedEncoding != null) {
      // it seems jetty may handle multi-value headers differently
      var stream =
          acceptedEncoding.size() > 1
              ? acceptedEncoding.stream()
              : Arrays.stream(acceptedEncoding.getFirst().split(","));

      return stream
          .map(e -> e.trim().split(";")[0])
          .map(e -> "*".equals(e) ? "gzip" : e.toLowerCase())
          .map(compression::forType)
          .filter(Objects::nonNull)
          .findFirst();
    }
    return Optional.empty();
  }
}
