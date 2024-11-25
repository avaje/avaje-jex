package io.avaje.jex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Predicate;

/** Need an entirely separate impl if using stuff like jlink */
final class ClassPathResourceHandler extends AbstractStaticHandler implements ExchangeHandler {

  private final Path indexFile;
  private final Path singleFile;

  ClassPathResourceHandler(
      String urlPrefix,
      String filesystemRoot,
      Map<String, String> mimeTypes,
      Map<String, String> headers,
      Predicate<Context> skipFilePredicate,
      Path indexFile,
      Path singleFile) {
    super(urlPrefix, filesystemRoot, mimeTypes, headers, skipFilePredicate);

    this.indexFile = indexFile;
    this.singleFile = singleFile;
  }

  @Override
  public void handle(Context ctx) throws IOException {

    if (singleFile != null) {
      sendPathIS(ctx, singleFile.toString(), singleFile);
      return;
    }

    final var jdkExchange = ctx.jdkExchange();
    if (skipFilePredicate.test(ctx)) {
      throw404(jdkExchange);
    }

    final String wholeUrlPath = jdkExchange.getRequestURI().getPath();

    if (wholeUrlPath.endsWith("/") || wholeUrlPath.equals(urlPrefix)) {
      sendPathIS(ctx, indexFile.toString(), indexFile);

      return;
    }

    final String urlPath = wholeUrlPath.substring(urlPrefix.length());

    Path path;
    try {
      path = Path.of(filesystemRoot, urlPath).toRealPath();

    } catch (IOException e) {
      // This may be more benign (i.e. not an attack, just a 403),
      // but we don't want an attacker to be able to discern the difference.
      reportPathTraversal();
      return;
    }

    String canonicalPath = path.toString();
    if (!canonicalPath.startsWith(filesystemRoot)) {
      reportPathTraversal();
    }

    sendPathIS(ctx, urlPath, path);
  }

  private void sendPathIS(Context ctx, String urlPath, Path path) throws IOException {
    var exchange = ctx.jdkExchange();
    String mimeType = lookupMime(urlPath);
    ctx.header("Content-Type", mimeType);
    ctx.headers(headers);
    exchange.sendResponseHeaders(200, Files.size(path));
    try (InputStream fis = Files.newInputStream(path);
        OutputStream os = exchange.getResponseBody()) {

      fis.transferTo(os);
    } catch (Exception e) {
      throw404(ctx.jdkExchange());
    }
  }
}
