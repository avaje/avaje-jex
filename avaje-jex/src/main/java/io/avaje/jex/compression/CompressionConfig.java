package io.avaje.jex.compression;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CompressionConfig {

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
   * set default gzip compressor level
   *
   * @param level the new compression level (0-9)
   */
  public void gzipCompressionLevel(int level) {
    compressors.put(GzipCompressor.ENCODING, new GzipCompressor(level));
  }

  public void disableCompression() {
    enabled = false;
    compressors.clear();
  }

  public int minSizeForCompression() {
    return minSizeForCompression;
  }

  public CompressionConfig minSizeForCompression(int minSizeForCompression) {
    this.minSizeForCompression = minSizeForCompression;
    if (minSizeForCompression < HTTP_PACKET_SIZE)
      throw new IllegalArgumentException(
          "Compression should only happen on payloads bigger than an http packect");
    return this;
  }

  public boolean compressionEnabled() {
    return enabled;
  }

  public boolean allowsForCompression(String contentType) {

    return contentType == null
        || allowedExcludedTypes.contains(contentType)
        || !excludedMimeTypes.contains(contentType)
            && !contentType.startsWith("image/")
            && !contentType.startsWith("audio/")
            && !contentType.startsWith("video/");
  }

  Compressor forType(String type) {
    return compressors.get(type.toLowerCase());
  }
}
