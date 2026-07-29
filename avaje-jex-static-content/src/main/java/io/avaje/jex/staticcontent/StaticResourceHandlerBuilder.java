package io.avaje.jex.staticcontent;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import io.avaje.jex.Jex;
import io.avaje.jex.compression.CompressionConfig;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.ExchangeHandler;
import io.avaje.jex.security.Role;
import io.avaje.jex.spi.ClassResourceLoader;

final class StaticResourceHandlerBuilder implements StaticContent.Builder, StaticContent {

  private static final String FAILED_TO_LOCATE_FILE = "Failed to locate file: ";
  private static final String DIRECTORY_INDEX_FAILURE =
      "Failed to locate Directory Index Resource: ";
  private static final String SPA_ROOT_FAILURE = "Failed to locate SPA Root Resource: ";
  private static final Predicate<Context> NO_OP_PREDICATE = ctx -> false;
  private static final ClassResourceLoader DEFAULT_LOADER =
      ClassResourceLoader.fromClass(StaticContent.class);

  private String path = "/";
  private String root;
  private String directoryIndex = null;
  private String spaRoot = null;
  private ClassResourceLoader resourceLoader = DEFAULT_LOADER;
  private final Map<String, String> mimeTypes = new HashMap<>();
  private final Map<String, String> headers = new HashMap<>();
  private Predicate<Context> skipFilePredicate = NO_OP_PREDICATE;
  private boolean isClasspath = true;
  private boolean precompress;
  private Role[] roles = {};

  private StaticResourceHandlerBuilder(String root) {
    this.root = root;
  }

  static StaticResourceHandlerBuilder builder(String root) {
    return new StaticResourceHandlerBuilder(root);
  }

  @Override
  public void apply(Jex jex) {

    path =
        Objects.requireNonNull(path)
            .transform(s -> path.endsWith("/") && directoryIndex != null ? path + "*" : path);

    var handlerPath = path;
    var handler = createHandler(jex.config().compression());
    jex.routing().get(handlerPath, handler, roles).head(handlerPath, handler, roles);
  }

  @Override
  public StaticContent build() {
    return this;
  }

  ExchangeHandler createHandler(CompressionConfig compress) {
    path =
        path.transform(this::prependSlash)
            .transform(s -> s.endsWith("/*") ? s.substring(0, s.length() - 2) : s);

    root = isClasspath ? root.transform(this::prependSlash) : root;
    if (isClasspath && "/".equals(root)) {
      throw new IllegalArgumentException(
          "Cannot serve full classpath, please configure a classpath prefix");
    }

    if (root.endsWith("/") && directoryIndex == null) {
      throw new IllegalArgumentException(
          "Directory Index file or SPA Root is required when serving directories");
    }

    if (!isClasspath) {
      return fileLoader(compress);
    }

    return classPathHandler(compress);
  }

  @Override
  public StaticResourceHandlerBuilder route(String path, Role... roles) {
    this.path = path;
    this.roles = roles;
    return this;
  }

  @Override
  public StaticResourceHandlerBuilder directoryIndex(String directoryIndex) {
    this.directoryIndex = directoryIndex;
    return this;
  }

  @Override
  public StaticResourceHandlerBuilder spaRoot(String spaIndex) {
    this.spaRoot = spaIndex;
    if (directoryIndex == null) {
      directoryIndex(spaIndex);
    }
    return this;
  }

  @Override
  public StaticResourceHandlerBuilder resourceLoader(ClassResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
    return this;
  }

  @Override
  public StaticResourceHandlerBuilder putMimeTypeMapping(String key, String value) {
    this.mimeTypes.put(key, value);
    return this;
  }

  @Override
  public StaticResourceHandlerBuilder putResponseHeader(String key, String value) {
    this.headers.put(key, value);
    return this;
  }

  @Override
  public StaticResourceHandlerBuilder skipFilePredicate(Predicate<Context> skipFilePredicate) {
    this.skipFilePredicate = skipFilePredicate;
    return this;
  }

  @Override
  public StaticResourceHandlerBuilder preCompress() {
    this.precompress = true;
    return this;
  }

  StaticResourceHandlerBuilder file() {
    this.isClasspath = false;
    return this;
  }

  private String prependSlash(String s) {
    return s.charAt(0) == '/' ? s : "/" + s;
  }

  private String appendSlash(String s) {
    return s.endsWith("/") ? s : s + "/";
  }

  private StaticFileHandler fileLoader(CompressionConfig compress) {
    String fsRoot;
    Path dirIndex = null;
    Path spaRootFile = null;
    Path singleFile = null;
    if (directoryIndex != null) {
      dirIndex = Path.of(root.transform(this::appendSlash) + directoryIndex).toAbsolutePath().normalize();
      fsRoot = dirIndex.getParent().toString();
      if (!Files.exists(dirIndex)) {
        throw new IllegalStateException(
            DIRECTORY_INDEX_FAILURE + root.transform(this::appendSlash) + directoryIndex);
      }
    } else {
      singleFile = Path.of(root).toAbsolutePath().normalize();
      fsRoot = singleFile.getParent().toString();
      if (!Files.exists(singleFile)) {
        throw new IllegalStateException(FAILED_TO_LOCATE_FILE + root);
      }
    }

    if (spaRoot != null) {
      spaRootFile = Path.of(root.transform(this::appendSlash) + spaRoot).toAbsolutePath().normalize();
      fsRoot = spaRootFile.getParent().toString();
      if (!Files.exists(spaRootFile)) {
        throw new IllegalStateException(
            SPA_ROOT_FAILURE + root.transform(this::appendSlash) + spaRoot);
      }
    }

    return new StaticFileHandler(
        path,
        fsRoot,
        mimeTypes,
        headers,
        skipFilePredicate,
        dirIndex,
        spaRootFile,
        singleFile,
        precompress,
        compress);
  }

  private StaticClassResourceHandler classPathHandler(CompressionConfig compress) {
    URL dirIndex = null;
    URL spaRootUrl = null;
    URL singleFile = null;
    if (directoryIndex != null) {
      dirIndex = resourceLoader.loadResource(root.transform(this::appendSlash) + directoryIndex);
    } else {
      singleFile = resourceLoader.loadResource(root);
    }

    if (spaRoot != null) {
      spaRootUrl = resourceLoader.loadResource(root.transform(this::appendSlash) + spaRoot);
    }

    return new StaticClassResourceHandler(
        path,
        root,
        mimeTypes,
        headers,
        skipFilePredicate,
        resourceLoader,
        dirIndex,
        spaRootUrl,
        singleFile,
        precompress,
        compress);
  }
}
