package io.avaje.jex;

import static io.avaje.jex.ResourceLocation.CLASS_PATH;
import static java.util.Map.entry;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import io.avaje.jex.core.CoreServiceLoader;
import io.avaje.jex.spi.StaticResourceLoader;

final class StaticResourceHandlerBuilder implements StaticContentConfig {

  static final Predicate<Context> NO_OP_PREDICATE = ctx -> false;
  private static final String TEXT_PLAIN = "text/plain";
  private static final Map<String, String> MIME_MAP =
      Map.ofEntries(
          entry("css", "text/css"),
          entry("gif", "image/gif"),
          entry("html", "text/html"),
          entry("js", "application/javascript"),
          entry("json", "application/json"),
          entry("jpg", "image/jpeg"),
          entry("jpeg", "image/jpeg"),
          entry("mp4", "video/mp4"),
          entry("pdf", "application/pdf"),
          entry("png", "image/png"),
          entry("svg", "image/svg+xml"),
          entry("xlsm", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
          entry("xml", "application/xml"),
          entry("zip", "application/zip"),
          entry("md", TEXT_PLAIN),
          entry("txt", TEXT_PLAIN),
          entry("php", TEXT_PLAIN));

  private String path = "/";
  private String root = "/public/";
  private String directoryIndex = null;
  private StaticResourceLoader resourceLoader = CoreServiceLoader.resourceLoader();
  private final Map<String, String> mimeTypes = new HashMap<>(MIME_MAP);
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

  private boolean nonFilePath(URI uri) {
    return !uri.getScheme().startsWith("file");
  }

  private ExchangeHandler fileLoader(Function<String, File> fileLoader) {
    String fsRoot;
    File welcomeFile = null;
    File singleFile = null;
    if (directoryIndex != null) {
      try {

        welcomeFile =
            fileLoader.apply(root.transform(this::appendSlash) + directoryIndex).getCanonicalFile();

        fsRoot = welcomeFile.getParentFile().getPath();
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
        path, fsRoot, mimeTypes, headers, skipFilePredicate, welcomeFile, singleFile);
  }

  private ExchangeHandler classPathHandler() {
    Function<String, URL> urlFunc = resourceLoader::getResourceURI;

    Function<String, URI> loaderFunc = urlFunc.andThen(this::toURI);

    if (directoryIndex != null) {
      try {
        var uri = loaderFunc.apply(root.transform(this::appendSlash) + directoryIndex);
        if (nonFilePath(uri)) {
          var dirIndex = Paths.get(uri).toRealPath();
          return new JrtResourceHandler(
              path,
              dirIndex.getParent().toString(),
              mimeTypes,
              headers,
              skipFilePredicate,
              dirIndex,
              null);
        }
        return fileLoader(loaderFunc.andThen(File::new));
      } catch (Exception e) {

        throw new IllegalStateException(
            "Failed to locate Directory Index Resource: %s"
                + root.transform(this::appendSlash)
                + directoryIndex,
            e);
      }
    }

    try {
      var uri = loaderFunc.apply(root);

      if (nonFilePath(uri)) {
        var singleFile = Paths.get(uri).toRealPath();
        return new JrtResourceHandler(
            path,
            singleFile.getParent().toString(),
            mimeTypes,
            headers,
            skipFilePredicate,
            null,
            singleFile);
      }

      return fileLoader(loaderFunc.andThen(File::new));
    } catch (Exception e) {

      throw new IllegalStateException(
          "Failed to locate Directory Index Resource: %s"
              + root.transform(this::appendSlash)
              + directoryIndex,
          e);
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
