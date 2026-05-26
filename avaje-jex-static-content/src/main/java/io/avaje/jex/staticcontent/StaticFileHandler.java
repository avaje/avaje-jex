package io.avaje.jex.staticcontent;

import static io.avaje.jex.core.Constants.CONTENT_TYPE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Predicate;

import com.sun.net.httpserver.HttpExchange;

import io.avaje.jex.compression.CompressionConfig;
import io.avaje.jex.http.Context;

final class StaticFileHandler extends AbstractStaticHandler {

  private final Path indexFile;
  private final Path spaRoot;
  private final Path singleFile;

  StaticFileHandler(
      String urlPrefix,
      String filesystemRoot,
      Map<String, String> mimeTypes,
      Map<String, String> headers,
      Predicate<Context> skipFilePredicate,
      Path welcomeFile,
      Path spaRoot,
      Path singleFile, boolean precompress,
      CompressionConfig compressionConfig) {
    super(
        urlPrefix,
        filesystemRoot,
        mimeTypes,
        headers,
        skipFilePredicate,
        precompress,
        compressionConfig);
    this.indexFile = welcomeFile;
    this.spaRoot = spaRoot;
    this.singleFile = singleFile;
  }

  @Override
  public void handle(Context ctx) throws IOException {
    final var jdkExchange = ctx.exchange();
    if (singleFile != null) {

      final var path = singleFile.toString();
      if (isCached(path) && writeCached(ctx, path)) {
        return;
      }

      sendFile(ctx, jdkExchange, path, singleFile);
      return;
    }

    if (skipFilePredicate.test(ctx)) {
      throw404(jdkExchange);
    }

    final String wholeUrlPath = jdkExchange.getRequestURI().getPath();
    if (wholeUrlPath.endsWith("/") || wholeUrlPath.equals(urlPrefix)) {

      final var path = indexFile.toString();
      if (isCached(path) && writeCached(ctx, path)) {
        return;
      }

      sendFile(ctx, jdkExchange, path, indexFile);
      return;
    }

    final String urlPath = wholeUrlPath.substring(urlPrefix.length());

    if (isCached(urlPath) && writeCached(ctx, urlPath)) {
      return;
    }

    // strip leading slash so Path.resolve() treats it as relative (File(parent,child) did this implicitly on Unix)
    // Path-aware startsWith guards against directory traversal more reliably than string prefix
    String relUrlPath = urlPath.startsWith("/") ? urlPath.substring(1) : urlPath;
    Path canonicalFile = Path.of(filesystemRoot).resolve(relUrlPath).normalize();
    if (!canonicalFile.startsWith(filesystemRoot)) {
      reportPathTraversal();
      return;
    }

    sendFile(ctx, jdkExchange, urlPath, canonicalFile);
  }

  private void sendFile(Context ctx, HttpExchange jdkExchange, String urlPath, Path canonicalFile)
      throws IOException {
    try (var fis = Files.newInputStream(canonicalFile)) {
      String mimeType = lookupMime(urlPath);
      ctx.header(CONTENT_TYPE, mimeType);
      ctx.headers(headers);
      if (precompress) {
        addCachedEntry(ctx, urlPath, fis);
        return;
      }

      if ("HEAD".equals(ctx.method())) {
        writeHeadResponse(ctx, fis);
        return;
      }

      ctx.rangedWrite(fis);
    } catch (NoSuchFileException e) {
      if (spaRoot != null) {
        final var path = spaRoot.toString();
        sendFile(ctx, jdkExchange, path, spaRoot);
        return;
      }
      throw404(jdkExchange);
    }
  }
}
