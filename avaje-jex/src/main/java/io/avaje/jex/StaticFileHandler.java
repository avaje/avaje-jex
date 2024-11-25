package io.avaje.jex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.function.Predicate;

import com.sun.net.httpserver.HttpExchange;

import io.avaje.jex.http.BadRequestException;
import io.avaje.jex.http.NotFoundException;

final class StaticFileHandler implements ExchangeHandler {
  private final Map<String, String> mimeTypes;
  private final String filesystemRoot;
  private final String urlPrefix;
  private final File welcomeFile;
  private final File singleFile;
  private final Predicate<Context> skipFilePredicate;
  private final Map<String, String> headers;

  StaticFileHandler(
      String urlPrefix,
      String filesystemRoot,
      Map<String, String> mimeTypes,
      Map<String, String> headers,
      Predicate<Context> skipFilePredicate,
      File welcomeFile,
      File singleFile) {

    this.filesystemRoot = filesystemRoot;
    this.urlPrefix = urlPrefix;
    this.welcomeFile = welcomeFile;
    this.singleFile = singleFile;
    this.skipFilePredicate = skipFilePredicate;
    this.headers = headers;
    this.mimeTypes = mimeTypes;
  }

  @Override
  public void handle(Context ctx) throws IOException {

    final var jdkExchange = ctx.jdkExchange();

    if (singleFile != null) {
      sendFile(ctx, jdkExchange, singleFile.getPath(), singleFile);
      return;
    }

    if (skipFilePredicate.test(ctx)) {
      throw404(jdkExchange);
    }

    final String wholeUrlPath = jdkExchange.getRequestURI().getPath();

    if (wholeUrlPath.endsWith("/") || wholeUrlPath.equals(urlPrefix)) {
      sendFile(ctx, jdkExchange, welcomeFile.getPath(), welcomeFile);

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

  private void throw404(final HttpExchange jdkExchange) {
    throw new NotFoundException("File Not Found for request: " + jdkExchange.getRequestURI());
  }

  private void sendFile(
      Context ctx, final HttpExchange jdkExchange, final String urlPath, File canonicalFile)
      throws IOException {
    try (var fis = new FileInputStream(canonicalFile)) {

      String mimeType = lookupMime(urlPath);
      ctx.header("Content-Type", mimeType);
      ctx.headers(headers);
      jdkExchange.sendResponseHeaders(200, canonicalFile.length());
      fis.transferTo(jdkExchange.getResponseBody());
    } catch (FileNotFoundException e) {
      throw404(jdkExchange);
    }
  }

  // This is one function to avoid giving away where we failed
  private static void reportPathTraversal() {
    throw new BadRequestException("Path traversal attempt detected");
  }

  private static String getExt(String path) {
    int slashIndex = path.lastIndexOf('/');
    String basename = (slashIndex < 0) ? path : path.substring(slashIndex + 1);

    int dotIndex = basename.lastIndexOf('.');
    if (dotIndex >= 0) {
      return basename.substring(dotIndex + 1);
    } else {
      return "";
    }
  }

  private String lookupMime(String path) {
    String ext = getExt(path).toLowerCase();
    return mimeTypes.getOrDefault(ext, "application/octet-stream");
  }
}
