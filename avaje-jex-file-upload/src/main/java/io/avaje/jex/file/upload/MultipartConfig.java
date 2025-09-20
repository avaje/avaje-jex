package io.avaje.jex.file.upload;

/** This class contains the configuration for handling multipart file uploads */
public class MultipartConfig {
  private String cacheDirectory = System.getProperty("java.io.tmpdir");
  private long maxFileSize = -1;
  private long maxRequestSize = -1;
  private int maxInMemoryFileSize = 1;

  MultipartConfig() {}

  /**
   * Sets the location of the cache directory used to write file uploads
   *
   * @param path : the path of the cache directory used to write file uploads > maxInMemoryFileSize
   */
  public void cacheDirectory(String path) {
    this.cacheDirectory = path;
  }

  /**
   * Sets the maximum file size for an individual file upload
   *
   * @param size : the maximum size of the file
   * @param sizeUnit : the units that this size is measured in
   */
  public void maxFileSize(long size, SizeUnit sizeUnit) {
    this.maxFileSize = size * sizeUnit.multiplier();
  }

  /**
   * Sets the maximum size for a single file before it will be cached to disk rather than read in
   * memory
   *
   * @param size : the maximum size of the file
   * @param sizeUnit : the units that this size is measured in
   */
  public void maxInMemoryFileSize(int size, SizeUnit sizeUnit) {
    this.maxInMemoryFileSize = size * sizeUnit.multiplier();
  }

  /**
   * Sets the maximum size for the entire multipart request
   *
   * @param size : the maximum size of the file
   * @param sizeUnit : the units that this size is measured in
   */
  public void maxRequestSize(long size, SizeUnit sizeUnit) {
    this.maxRequestSize = size * sizeUnit.multiplier();
  }

  /**
   * This enum represents the potential file size descriptors to avoid the use of hard-coded
   * multipliers
   */
  public enum SizeUnit {
    BYTES(1),
    KB(1024),
    MB(1024 * 1024),
    GB(1024 * 1024 * 1024);

    private final int multiplier;

    SizeUnit(int multiplier) {
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
