package io.avaje.jex.file.upload;

/**
 * Configuration settings for handling multipart file uploads.
 *
 * <p>This class allows you to customize various aspects of file upload behavior, such as file size
 * limits and the location where temporary files are stored.
 */
public final class MultipartConfig {
  private String cacheDirectory = System.getProperty("java.io.tmpdir");
  private long maxFileSize = -1;
  private long maxRequestSize = -1;
  private int maxInMemoryFileSize = 1;

  MultipartConfig() {}

  /**
   * Sets the directory where uploaded files exceeding the in-memory size limit will be cached.
   *
   * <p>If not set, the java's default temporary directory will be used.
   *
   * @param path The absolute path to the cache directory.
   * @see #maxInMemoryFileSize(int, FileSize)
   */
  public void cacheDirectory(String path) {
    this.cacheDirectory = path;
  }

  /**
   * Sets the maximum allowed size for a single uploaded file.
   *
   * <p>A value of -1 indicates no limit.
   *
   * @param size The maximum size of the file.
   * @param sizeUnit The unit of measurement for the size (e.g., KB, MB, GB).
   */
  public void maxFileSize(long size, FileSize sizeUnit) {
    this.maxFileSize = size * sizeUnit.multiplier();
  }

  /**
   * Sets the maximum size a file can be before it is written to disk.
   *
   * <p>A value of -1 indicates no limit.
   *
   * <p>Files smaller than this size will be kept in memory, which can be faster for small uploads
   * but consumes more memory. Files larger than this size will be written to the {@link
   * #cacheDirectory(String)}. A value of 0 means all files are written to disk.
   *
   * @param size The maximum in-memory size of the file.
   * @param sizeUnit The unit of measurement for the size (e.g., KB, MB).
   */
  public void maxInMemoryFileSize(int size, FileSize sizeUnit) {
    this.maxInMemoryFileSize = size * sizeUnit.multiplier();
  }

  /**
   * Sets the maximum total size for a multipart request, including all files and form data.
   *
   * <p>A value of -1 indicates no limit.
   *
   * @param size The maximum size of the entire request.
   * @param sizeUnit The unit of measurement for the size (e.g., KB, MB, GB).
   */
  public void maxRequestSize(long size, FileSize sizeUnit) {
    this.maxRequestSize = size * sizeUnit.multiplier();
  }

  /** Represents standard file size units for use in configuration. */
  public enum FileSize {
    BYTES(1),
    KB(1024),
    MB(1024 * 1024),
    GB(1024 * 1024 * 1024);

    private final int multiplier;

    FileSize(int multiplier) {
      this.multiplier = multiplier;
    }

    int multiplier() {
      return multiplier;
    }
  }

  String cacheDirectory() {
    return cacheDirectory;
  }

  long maxFileSize() {
    return maxFileSize;
  }

  long maxRequestSize() {
    return maxRequestSize;
  }

  int maxInMemoryFileSize() {
    return maxInMemoryFileSize;
  }
}
