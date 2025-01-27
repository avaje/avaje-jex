package io.avaje.jex.compression;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import io.avaje.jex.Context;
import io.avaje.jex.core.Constants;

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
          compressedStream == null && compression.allowsForCompression(ctx.contentType());

      if (compressionAllowed && length >= minSizeForCompression) {
        Optional<Compressor> compressor;
        compressor = findMatchingCompressor(ctx.header(Constants.ACCEPT_ENCODING));
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
    }
    originStream.close();
  }

  private Optional<Compressor> findMatchingCompressor(String acceptedEncoding) {
    if (acceptedEncoding != null) {
      return Arrays.stream(acceptedEncoding.split(","))
          .map(e -> e.trim().split(";")[0])
          .map(e -> "*".equals(e) ? "gzip" : e.toLowerCase())
          .map(compression::forType)
          .filter(Objects::nonNull)
          .findFirst();
    }
    return Optional.empty();
  }
}
