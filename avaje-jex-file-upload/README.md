# File Uploads

[![Maven Central](https://img.shields.io/maven-central/v/io.avaje/avaje-jex-file-upload.svg?label=Maven%20Central)](https://mvnrepository.com/artifact/io.avaje/avaje-jex-file-upload)
[![javadoc](https://javadoc.io/badge2/io.avaje/avaje-jex-file-upload/javadoc.svg?color=purple)](https://javadoc.io/doc/io.avaje/avaje-jex-file-upload)

Module for handling file uploads via multipart form data.

It provides a `FileUploadPlugin` to configure file upload behavior and a `FileUploadService` to access uploaded files in your route handlers.

## Installation

Add the file upload dependency to your project:
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-jex-file-upload</artifactId>
  <version>${avaje.jex.version}</version>
</dependency>
```

## Basic Usage

### Default Configuration
```java
// With default configuration
Jex app = Jex.create().plugin(FileUploadPlugin.create());
```

### Custom Configuration
```java
// With custom configuration
FileUploadPlugin uploadPlugin =
    FileUploadPlugin.create(
        config ->
            config
                .cacheDirectory("/tmp/uploads")
                .maxFileSize(10, MB)
                .maxRequestSize(50, MB)
                .maxInMemoryFileSize(1, MB));

Jex app = Jex.create().plugin(uploadPlugin);
```

## Accessing Uploaded Files

Access uploaded files in your routes using the `FileUploadService`:
```java
app.post(
    "/upload",
    ctx -> {
      var uploadService = ctx.attribute(FileUploadService.class);

      // Get a single file
      MultiPart file = uploadService.uploadedFile("avatar");

      // Get multiple files with the same field name
      List<MultiPart> files = uploadService.uploadedFiles("documents");

      // Get all uploaded files
      List<MultiPart> allFiles = uploadService.uploadedFiles();

      // Get files grouped by field name
      Map<String, List<MultiPart>> fileMap = uploadService.uploadedFileMap();
    });
```

## Configuration Options

| Method | Description |
|--------|-------------|
| `cacheDirectory("/path/to/dir")` | Sets the directory where uploaded files exceeding the in-memory size limit will be cached. Defaults to the system's temporary directory. |
| `maxFileSize(size, unit)` | Sets the maximum allowed size for a single uploaded file. Use -1 for no limit. |
| `maxRequestSize(size, unit)` | Sets the maximum total size for a multipart request, including all files and form data. Use -1 for no limit. |
| `maxInMemoryFileSize(size, unit)` | Sets the maximum size a file can be before it is written to disk. Files smaller than this are kept in memory. Use 0 to write all files to disk. |

### File Size Units

Available units: `BYTES`, `KB`, `MB`, `GB`
