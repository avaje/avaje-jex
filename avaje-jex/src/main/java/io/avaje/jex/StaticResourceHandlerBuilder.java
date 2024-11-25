package io.avaje.jex;

import static io.avaje.jex.ResourceLocation.CLASS_PATH;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import io.avaje.jex.core.CoreServiceLoader;
import io.avaje.jex.spi.StaticResourceLoader;

final class StaticResourceHandlerBuilder implements StaticContentConfig {

  static final Predicate<Context> NO_OP_PREDICATE = ctx -> false;

  private String path = "/";
  private String root = "/public/";
  private String directoryIndex = null;
  private StaticResourceLoader resourceLoader = CoreServiceLoader.resourceLoader();
  private final Map<String, String> mimeTypes = new HashMap<>();
  private final Map<String, String> headers = new HashMap<>();
  private Predicate<Context> skipFilePredicate = NO_OP_PREDICATE;
  private ResourceLocation location = CLASS_PATH;

  private StaticResourceHandlerBuilder() {}

  public static StaticResourceHandlerBuilder builder() {
    return new StaticResourceHandlerBuilder();
  }

  @Override
  public ExchangeHandler createHandler() {

    path =
        Objects.requireNonNull(path)
            .transform(this::prependSlash)
            .transform(s -> s.endsWith("/*") ? s.substring(0, s.length() - 2) : s);

    final var isClasspath = location == CLASS_PATH;

    root = isClasspath ? root.transform(this::prependSlash) : root;
    if (isClasspath && "/".equals(root)) {
      throw new IllegalArgumentException(
          "Cannot serve full classpath, please configure a classpath prefix");
    }

    if (root.endsWith("/") && directoryIndex == null) {
      throw new IllegalArgumentException(
          "Directory Index file is required when serving directories");
    }

    if (location == ResourceLocation.FILE) {
      return fileLoader(File::new);
    }

    return classPathHandler();
  }

  @Override
  public StaticResourceHandlerBuilder httpPath(String path) {
    this.path = path;
    return this;
  }

  @Override
  public String httpPath() {
    return path;
  }

  @Override
  public StaticResourceHandlerBuilder resource(String directory) {
    this.root = directory;
    return this;
  }

  @Override
  public StaticResourceHandlerBuilder directoryIndex(String directoryIndex) {
    this.directoryIndex = directoryIndex;
    return this;
  }

  @Override
  public StaticResourceHandlerBuilder resourceLoader(StaticResourceLoader resourceLoader) {
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
  public StaticResourceHandlerBuilder location(ResourceLocation location) {
    this.location = location;
    return this;
  }

  private String prependSlash(String s) {
    return s.startsWith("/") ? s : "/" + s;
  }

  private String appendSlash(String s) {
    return s.endsWith("/") ? s : s + "/";
  }

  private ExchangeHandler fileLoader(Function<String, File> fileLoader) {
    String fsRoot;
    File dirIndex = null;
    File singleFile = null;
    if (directoryIndex != null) {
      try {

        dirIndex =
            fileLoader.apply(root.transform(this::appendSlash) + directoryIndex).getCanonicalFile();

        fsRoot = dirIndex.getParentFile().getPath();
      } catch (Exception e) {
        throw new IllegalStateException(
            "Failed to locate Directory Index Resource: "
                + root.transform(this::appendSlash)
                + directoryIndex,
            e);
      }
    } else {
      try {

        singleFile = fileLoader.apply(root).getCanonicalFile();

        fsRoot = singleFile.getParentFile().getPath();
      } catch (Exception e) {
        throw new IllegalStateException("Failed to locate File: " + root, e);
      }
    }

    return new StaticFileHandler(
        path, fsRoot, mimeTypes, headers, skipFilePredicate, dirIndex, singleFile);
  }

  private ExchangeHandler classPathHandler() {
    Function<String, URL> urlFunc = resourceLoader::getResourceURI;

    Function<String, URI> loaderFunc = urlFunc.andThen(this::toURI);
    String fsRoot;
    Path dirIndex = null;
    Path singleFile = null;
    if (directoryIndex != null) {
      try {
        var uri = loaderFunc.apply(root.transform(this::appendSlash) + directoryIndex);

        initJarFS(uri);

        dirIndex = Paths.get(uri).toRealPath();
        fsRoot = Paths.get(uri).getParent().toString();

      } catch (Exception e) {

        throw new IllegalStateException(
            "Failed to locate Directory Index Resource: "
                + root.transform(this::appendSlash)
                + directoryIndex,
            e);
      }
    } else {
      try {
        var uri = loaderFunc.apply(root);

        initJarFS(uri);

        singleFile = Paths.get(uri).toRealPath();

        fsRoot = singleFile.getParent().toString();

      } catch (Exception e) {

        throw new IllegalStateException(
            "Failed to locate Directory Index Resource: "
                + root.transform(this::appendSlash)
                + directoryIndex,
            e);
      }
    }

    return new ClassPathResourceHandler(
        path, fsRoot, mimeTypes, headers, skipFilePredicate, dirIndex, singleFile);
  }

  private void initJarFS(URI uri) throws IOException {
    if ("jar".equals(uri.getScheme())) {
      for (var provider : FileSystemProvider.installedProviders()) {
        if ("jar".equalsIgnoreCase(provider.getScheme())) {
          try {
            provider.getFileSystem(uri);
          } catch (FileSystemNotFoundException e) {
            // in this case we need to initialize it first:
            provider.newFileSystem(uri, Collections.emptyMap());
          }
        }
      }
    }
  }

  private URI toURI(URL url) {

    try {
      return url.toURI();
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }
}
