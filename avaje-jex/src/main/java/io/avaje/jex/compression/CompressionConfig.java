package io.avaje.jex.compression;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Configuration class for compression settings. */
public final class CompressionConfig {

  private static final int HTTP_PACKET_SIZE = 1500;

  private static final Set<String> excludedMimeTypes =
      Set.of(
          "application/compress",
          "application/zip",
          "application/gzip",
          "application/bzip2",
          "application/brotli",
          "application/x-xz",
          "application/x-rar-compressed");

  private boolean enabled = true;

  private int minSizeForCompression = HTTP_PACKET_SIZE;

  private final Map<String, Compressor> compressors =
      new HashMap<>(Map.of(GzipCompressor.ENCODING, new GzipCompressor()));

  private final Set<String> allowedExcludedTypes = Set.of("image/svg+xml");

  /**
   * Sets the default GZIP compression level.
   *
   * @param level The new compression level (0-9).
   */
  public void gzipCompressionLevel(int level) {
    compressors.put(GzipCompressor.ENCODING, new GzipCompressor(level));
  }

  /** Disables compression. */
  public void disableCompression() {
    enabled = false;
    compressors.clear();
  }

  /**
   * Gets the minimum size for compression.
   *
   * @return The minimum size for compression.
   */
  public int minSizeForCompression() {
    return minSizeForCompression;
  }

  /**
   * Sets the minimum size for compression and returns the updated configuration.
   *
   * @param minSizeForCompression The new minimum size for compression.
   * @return The updated configuration.
   * @throws IllegalArgumentException If the minimum size is less than a network packet size.
   */
  public CompressionConfig minSizeForCompression(int minSizeForCompression) {
    this.minSizeForCompression = minSizeForCompression;
    if (minSizeForCompression < HTTP_PACKET_SIZE) {
      throw new IllegalArgumentException(
          "Compression should only happen on payloads bigger than an http packet");
    }
    return this;
  }

  /**
   * Checks if compression is enabled.
   *
   * @return True if compression is enabled, false otherwise.
   */
  public boolean compressionEnabled() {
    return enabled;
  }

  /**
   * Determines if a given content type is allowed for compression.
   *
   * @param contentType The content type to check.
   * @return True if the content type is allowed for compression, false otherwise.
   */
  public boolean allowsForCompression(String contentType) {
    return contentType == null
        || allowedExcludedTypes.contains(contentType)
        || !excludedMimeTypes.contains(contentType)
            && !contentType.startsWith("image/")
            && !contentType.startsWith("audio/")
            && !contentType.startsWith("video/");
  }

  /**
   * Gets the appropriate compressor for a given encoding type.
   *
   * @param encoding The Content-Encoding value.
   * @return The compressor for the given Content-Encoding value, or null if not found.
   */
  Compressor forType(String encoding) {
    return compressors.get(encoding.toLowerCase());
  }
}
