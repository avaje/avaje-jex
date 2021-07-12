package io.avaje.jex;

/**
 * Configuration for server handling of Multipart file uploads etc.
 */
public class UploadConfig {

  private String location;
  private long maxFileSize;
  private long maxRequestSize;
  private int fileSizeThreshold;

  public UploadConfig() {
  }

  public UploadConfig(String location, long maxFileSize, long maxRequestSize, int fileSizeThreshold) {
    this.location = location;
    this.maxFileSize = maxFileSize;
    this.maxRequestSize = maxRequestSize;
    this.fileSizeThreshold = fileSizeThreshold;
  }

  public String location() {
    return location;
  }

  public UploadConfig location(String location) {
    this.location = location;
    return this;
  }

  public long maxFileSize() {
    return maxFileSize;
  }

  public UploadConfig maxFileSize(long maxFileSize) {
    this.maxFileSize = maxFileSize;
    return this;
  }

  public long maxRequestSize() {
    return maxRequestSize;
  }

  public UploadConfig maxRequestSize(long maxRequestSize) {
    this.maxRequestSize = maxRequestSize;
    return this;
  }

  public int fileSizeThreshold() {
    return fileSizeThreshold;
  }

  public UploadConfig fileSizeThreshold(int fileSizeThreshold) {
    this.fileSizeThreshold = fileSizeThreshold;
    return this;
  }
}
