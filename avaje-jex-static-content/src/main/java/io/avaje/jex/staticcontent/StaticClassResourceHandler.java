package io.avaje.jex.staticcontent;

import static io.avaje.jex.core.Constants.CONTENT_TYPE;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Predicate;

import io.avaje.jex.compression.CompressionConfig;
import io.avaje.jex.http.Context;
import io.avaje.jex.spi.ClassResourceLoader;

final class StaticClassResourceHandler extends AbstractStaticHandler {

  private final URL indexFile;
  private final URL singleFile;
  private final ClassResourceLoader resourceLoader;

  StaticClassResourceHandler(
      String urlPrefix,
      String filesystemRoot,
      Map<String, String> mimeTypes,
      Map<String, String> headers,
      Predicate<Context> skipFilePredicate,
      ClassResourceLoader resourceLoader,
      URL indexFile,
      URL singleFile,
      boolean precompress,
      CompressionConfig compressionConfig) {
    super(
        urlPrefix,
        filesystemRoot,
        mimeTypes,
        headers,
        skipFilePredicate,
        precompress,
        compressionConfig);

    this.resourceLoader = resourceLoader;
    this.indexFile = indexFile;
    this.singleFile = singleFile;
  }

  @Override
  public void handle(Context ctx) {
    if (singleFile != null) {
      final var path = singleFile.getPath();
      if (isCached(path) && writeCached(ctx, path)) {
        return;
      }
      sendURL(ctx, path, singleFile);
      return;
    }

    final var jdkExchange = ctx.exchange();
    if (skipFilePredicate.test(ctx)) {
      throw404(jdkExchange);
    }

    final String wholeUrlPath = jdkExchange.getRequestURI().getPath();
    if (wholeUrlPath.endsWith("/") || wholeUrlPath.equals(urlPrefix)) {
      final var path = indexFile.getPath();
      if (isCached(path) && writeCached(ctx, path)) {
        return;
      }
      sendURL(ctx, path, indexFile);
      return;
    }

    final String urlPath = wholeUrlPath.substring(urlPrefix.length());

    if (isCached(urlPath)) {
      writeCached(ctx, urlPath);
      return;
    }

    final String normalizedPath =
        Paths.get(filesystemRoot, urlPath).normalize().toString().replace("\\", "/");

    if (!normalizedPath.startsWith(filesystemRoot)) {
      reportPathTraversal();
    }
    sendURL(ctx, urlPath, resourceLoader.loadResource(normalizedPath));
  }

  private void sendURL(Context ctx, String urlPath, URL path) {
    try (var fis = path.openStream()) {
      ctx.header(CONTENT_TYPE, lookupMime(urlPath));
      ctx.headers(headers);
      if (precompress) {
        addCachedEntry(ctx, urlPath, fis);
        return;
      }
      ctx.rangedWrite(fis);
    } catch (final IOException e) {
      throw404(ctx.exchange());
    }
  }
}
