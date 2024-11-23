package io.avaje.jex;

/** Configuration for server handling of Multipart file uploads etc. */
public record UploadConfig(
    String location, long maxFileSize, long maxRequestSize, int fileSizeThreshold) {}
