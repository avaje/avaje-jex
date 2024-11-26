package io.avaje.jex.compression;

import java.io.IOException;
import java.io.OutputStream;

/** Compressor interface defines methods for compressing an output stream. */
public interface Compressor {

  /**
   * Gets the content encoding for this compressor (e.g., "gzip").
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Encoding">MDN
   *     Content-Encoding</a>
   * @return the content encoding
   */
  String encoding();

  /**
   * Compresses the provided output stream.
   *
   * @param out the output stream to compress
   * @return the compressed output stream
   * @throws IOException if an error occurs during compression
   */
  OutputStream compress(OutputStream out) throws IOException;
}
