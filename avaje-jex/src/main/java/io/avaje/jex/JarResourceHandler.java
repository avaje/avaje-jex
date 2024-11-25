package io.avaje.jex;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Predicate;

final class JarResourceHandler extends AbstractStaticHandler implements ExchangeHandler {

  private final URL indexFile;
  private final URL singleFile;
  private final ClassResourceLoader resourceLoader;

  JarResourceHandler(
      String urlPrefix,
      String filesystemRoot,
      Map<String, String> mimeTypes,
      Map<String, String> headers,
      Predicate<Context> skipFilePredicate,
      ClassResourceLoader resourceLoader,
      URL indexFile,
      URL singleFile) {
    super(urlPrefix, filesystemRoot, mimeTypes, headers, skipFilePredicate);

    this.resourceLoader = resourceLoader;
    this.indexFile = indexFile;
    this.singleFile = singleFile;
  }

  @Override
  public void handle(Context ctx) throws IOException {

    final var jdkExchange = ctx.jdkExchange();

    if (singleFile != null) {
      sendURL(ctx, singleFile.getPath(), singleFile);
      return;
    }

    if (skipFilePredicate.test(ctx)) {
      throw404(jdkExchange);
    }

    final String wholeUrlPath = jdkExchange.getRequestURI().getPath();

    if (wholeUrlPath.endsWith("/") || wholeUrlPath.equals(urlPrefix)) {
      sendURL(ctx, indexFile.getPath(), indexFile);
      return;
    }

    final String urlPath = wholeUrlPath.substring(urlPrefix.length());

    final String normalizedPath =
        Paths.get(filesystemRoot, urlPath).normalize().toString().replace("\\", "/");

    if (!normalizedPath.startsWith(filesystemRoot)) {
      reportPathTraversal();
    }

    try (var fis = resourceLoader.getResourceAsStream(normalizedPath)) {
      ctx.header("Content-Type", lookupMime(normalizedPath));
      ctx.headers(headers);
      ctx.write(fis);
    } catch (final Exception e) {
      throw404(ctx.jdkExchange());
    }
  }

  private void sendURL(Context ctx, String urlPath, URL path) throws IOException {

    try (var fis = path.openStream()) {
      ctx.header("Content-Type", lookupMime(urlPath));
      ctx.headers(headers);
      ctx.write(fis);
    } catch (final Exception e) {
      throw404(ctx.jdkExchange());
    }
  }
}
