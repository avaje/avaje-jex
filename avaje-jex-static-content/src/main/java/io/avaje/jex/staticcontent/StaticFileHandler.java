package io.avaje.jex.staticcontent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.function.Predicate;

import com.sun.net.httpserver.HttpExchange;

import io.avaje.jex.Context;

final class StaticFileHandler extends AbstractStaticHandler {

  private final File indexFile;
  private final File singleFile;

  StaticFileHandler(
      String urlPrefix,
      String filesystemRoot,
      Map<String, String> mimeTypes,
      Map<String, String> headers,
      Predicate<Context> skipFilePredicate,
      File welcomeFile,
      File singleFile) {
    super(urlPrefix, filesystemRoot, mimeTypes, headers, skipFilePredicate);
    this.indexFile = welcomeFile;
    this.singleFile = singleFile;
  }

  @Override
  public void handle(Context ctx) throws IOException {
    final var jdkExchange = ctx.exchange();
    if (singleFile != null) {
      sendFile(ctx, jdkExchange, singleFile.getPath(), singleFile);
      return;
    }

    if (skipFilePredicate.test(ctx)) {
      throw404(jdkExchange);
    }

    final String wholeUrlPath = jdkExchange.getRequestURI().getPath();
    if (wholeUrlPath.endsWith("/") || wholeUrlPath.equals(urlPrefix)) {
      sendFile(ctx, jdkExchange, indexFile.getPath(), indexFile);
      return;
    }

    final String urlPath = wholeUrlPath.substring(urlPrefix.length());
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
      ctx.header("Content-Type", mimeType);
      ctx.headers(headers);
      ctx.write(fis);
    } catch (FileNotFoundException e) {
      throw404(jdkExchange);
    }
  }
}
