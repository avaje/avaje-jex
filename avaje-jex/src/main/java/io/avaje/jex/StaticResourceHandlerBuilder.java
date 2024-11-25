package io.avaje.jex;

import static io.avaje.jex.ResourceLocation.CLASS_PATH;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

final class StaticResourceHandlerBuilder implements StaticContentConfig {

  private static final String FAILED_TO_LOCATE_FILE = "Failed to locate file: ";
  private static final String DIRECTORY_INDEX_FAILURE =
      "Failed to locate Directory Index Resource: ";
  private static final Predicate<Context> NO_OP_PREDICATE = ctx -> false;
  private static final ClassResourceLoader DEFALT_LOADER =
      ClassResourceLoader.fromClass(StaticResourceHandlerBuilder.class);

  private String path = "/";
  private String root = "/public/";
  private String directoryIndex = null;
  private ClassResourceLoader resourceLoader = DEFALT_LOADER;
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
            DIRECTORY_INDEX_FAILURE + root.transform(this::appendSlash) + directoryIndex, e);
      }
    } else {
      try {

        singleFile = fileLoader.apply(root).getCanonicalFile();

        fsRoot = singleFile.getParentFile().getPath();
      } catch (Exception e) {
        throw new IllegalStateException(FAILED_TO_LOCATE_FILE + root, e);
      }
    }

    return new StaticFileHandler(
        path, fsRoot, mimeTypes, headers, skipFilePredicate, dirIndex, singleFile);
  }

  private ExchangeHandler classPathHandler() {
    Function<String, URL> urlFunc = resourceLoader::getResource;

    Function<String, URI> loaderFunc = urlFunc.andThen(this::toURI);
    String fsRoot;
    Path dirIndex = null;
    Path singleFile = null;
    if (directoryIndex != null) {
      try {
        var uri = loaderFunc.apply(root.transform(this::appendSlash) + directoryIndex);

        if ("jar".equals(uri.getScheme())) {

          var url = uri.toURL();
          return jarLoader(url.toString().transform(this::getJARRoot), url, null);
        }
        dirIndex = Paths.get(uri).toRealPath();
        fsRoot = Paths.get(uri).getParent().toString();

      } catch (Exception e) {

        throw new IllegalStateException(
            DIRECTORY_INDEX_FAILURE + root.transform(this::appendSlash) + directoryIndex, e);
      }
    } else {
      try {
        var uri = loaderFunc.apply(root);

        if ("jar".equals(uri.getScheme())) {

          var url = uri.toURL();

          return jarLoader(url.toString().transform(this::getJARRoot), null, uri.toURL());
        }

        singleFile = Paths.get(uri).toRealPath();

        fsRoot = singleFile.getParent().toString();

      } catch (Exception e) {

        throw new IllegalStateException(FAILED_TO_LOCATE_FILE + root, e);
      }
    }

    return new PathResourceHandler(
        path, fsRoot, mimeTypes, headers, skipFilePredicate, dirIndex, singleFile);
  }

  private String getJARRoot(String s) {
    return s.substring(0, s.lastIndexOf("/")).substring(s.indexOf("jar!") + 4);
  }

  private ExchangeHandler jarLoader(String fsRoot, URL dirIndex, URL singleFile) {

    return new JarResourceHandler(
        path, fsRoot, mimeTypes, headers, skipFilePredicate, resourceLoader, dirIndex, singleFile);
  }

  private URI toURI(URL url) {

    try {

      return url.toURI();
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }
}
