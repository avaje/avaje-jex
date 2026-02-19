package io.avaje.jex.staticcontent;

import static io.avaje.jex.core.Constants.CONTENT_TYPE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.function.Predicate;

import com.sun.net.httpserver.HttpExchange;

import io.avaje.jex.compression.CompressionConfig;
import io.avaje.jex.http.Context;

final class StaticFileHandler extends AbstractStaticHandler {

  private final File indexFile;
  private final File spaRoot;
  private final File singleFile;

  StaticFileHandler(
      String urlPrefix,
      String filesystemRoot,
      Map<String, String> mimeTypes,
      Map<String, String> headers,
      Predicate<Context> skipFilePredicate,
      File welcomeFile,
      File spaRoot,
      File singleFile, boolean precompress,
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

      final var path = singleFile.getPath();
      if (isCached(path)) {
        writeCached(ctx, path);
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

      final var path = indexFile.getPath();
      if (isCached(path)) {
        writeCached(ctx, path);
        return;
      }

      sendFile(ctx, jdkExchange, path, indexFile);
      return;
    }

    final String urlPath = wholeUrlPath.substring(urlPrefix.length());

    if (isCached(urlPath)) {
      writeCached(ctx, urlPath);
      return;
    }

    File canonicalFile;
    try {
      canonicalFile = new File(filesystemRoot, urlPath).getCanonicalFile();
    } catch (IOException e) {
      // This may be more benign (i.e. not an attack, just a 403),
      // but we don't want an attacker to be able to discern the difference.
      reportPathTraversal();
      return;
    }

    String canonicalPath = canonicalFile.getPath();
    if (!canonicalPath.startsWith(filesystemRoot)) {
      reportPathTraversal();
    }

    sendFile(ctx, jdkExchange, urlPath, canonicalFile);
  }

  private void sendFile(Context ctx, HttpExchange jdkExchange, String urlPath, File canonicalFile)
      throws IOException {
    try (var fis = new FileInputStream(canonicalFile)) {
      String mimeType = lookupMime(urlPath);
      ctx.header(CONTENT_TYPE, mimeType);
      ctx.headers(headers);
      if (precompress) {
        addCachedEntry(ctx, urlPath, fis);
        return;
      }
      ctx.rangedWrite(fis);
    } catch (FileNotFoundException e) {
      if (spaRoot != null) {
        final var path = spaRoot.getPath();
        sendFile(ctx, jdkExchange, path, spaRoot);
        return;
      }
      throw404(jdkExchange);
    }
  }
}
