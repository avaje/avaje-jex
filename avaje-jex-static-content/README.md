# Static Resources

[![Maven Central](https://img.shields.io/maven-central/v/io.avaje/avaje-jex-static-content.svg?label=Maven%20Central)](https://mvnrepository.com/artifact/io.avaje/avaje-jex-static-content)
[![javadoc](https://javadoc.io/badge2/io.avaje/avaje-jex-static-content/javadoc.svg?color=purple)](https://javadoc.io/doc/io.avaje/avaje-jex-static-content)

PLugin for serving static resources from the classpath or filesystem.

It provides a `StaticContent` class to configure the location and HTTP path of your static resources, as well as other attributes.

## Installation

Add the static content dependency to your project:
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-jex-static-content</artifactId>
  <version>${avaje.jex.version}</version>
</dependency>
```

## Basic Usage
```java
StaticContent singleFile =
    StaticContent.ofFile("src/main/resources/example.txt").httpPath("/single").build();
StaticContent directoryCP =
    StaticContent.ofClassPath("/public/").httpPath("/").directoryIndex("index.html").build();
Jex app =
    Jex.create()
        .plugin(singleFile) // will serve the src/main/resources/example.txt
        .plugin(directoryCP); // will serve files from the /public classpath directory
```

## Configuration Options

| Method | Description |
|--------|-------------|
| `directoryIndex("index.html")` | The index file to be served when a directory is requested. |
| `route("/public")` | Sets the HTTP path and security role for the static resource handler. |
| `preCompress()` | Sent resources will be pre-compressed and cached in memory when this is enabled. |
| `putMimeTypeMapping("sus", "application/sus")` | Adds a custom file extension MIME mapping to the configuration. |
| `putResponseHeader("key", value)` | Adds a new response header to the configuration. |
| `resourceLoader(clazz)` | Sets a custom resource loader for loading class/module path resources. |
| `skipFilePredicate(ctx -> !ctx.path().contains("/skip"))` | Sets a predicate to filter files based on the request context. |
