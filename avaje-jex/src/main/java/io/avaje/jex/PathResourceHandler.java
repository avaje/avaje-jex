package io.avaje.jex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Predicate;

import io.avaje.jex.spi.SpiContext;

final class PathResourceHandler extends AbstractStaticHandler implements ExchangeHandler {

  private final Path indexFile;
  private final Path singleFile;

  PathResourceHandler(
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

    final var jdkExchange = ctx.exchange();
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

    } catch (final IOException e) {
      // This may be more benign (i.e. not an attack, just a 403),
      // but we don't want an attacker to be able to discern the difference.
      reportPathTraversal();
      return;
    }

    final String canonicalPath = path.toString();
    if (!canonicalPath.startsWith(filesystemRoot)) {
      reportPathTraversal();
    }

    sendPathIS(ctx, urlPath, path);
  }

  private void sendPathIS(Context ctx, String urlPath, Path path) throws IOException {
    final var exchange = ctx.exchange();
    final String mimeType = lookupMime(urlPath);
    ctx.header("Content-Type", mimeType);
    ctx.headers(headers);

    try (var fis = Files.newInputStream(path);
        var os = exchange.getResponseBody()) {
      var spiCtx = (SpiContext) ctx;

      if (!spiCtx.compressionEnabled()) {
        exchange.sendResponseHeaders(200, Files.size(path));
        fis.transferTo(os);
      } else {
        spiCtx.write(fis);
      }
    } catch (final Exception e) {
      throw404(ctx.exchange());
    }
  }
}
