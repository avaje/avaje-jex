package io.avaje.jex.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

import io.avaje.jex.http.Context;
import io.avaje.jex.http.HttpStatus;

final class RangeWriter {

  private static final int DEFAULT_BUFFER_SIZE = 16384;

  static void write(Context ctx, InputStream inputStream, long totalBytes, long chunkSize) {

    ctx.header(Constants.ACCEPT_RANGES, "bytes");
    final String rangeHeader = ctx.header(Constants.RANGE);
    if (rangeHeader == null) {
      ctx.write(inputStream);
      return;
    }
    final List<String> requestedRange =
        Arrays.stream(rangeHeader.split("=")[1].split("-")).filter(s -> !s.isEmpty()).toList();
    final long from = Long.parseLong(requestedRange.get(0));
    final long to;

    final boolean audioOrVideo = isAudioOrVideo(ctx.responseHeader(Constants.CONTENT_TYPE));

    if (!audioOrVideo || from + chunkSize > totalBytes) {
      to = totalBytes - 1; // chunk bigger than file, write all
    } else if (requestedRange.size() == 2) {
      to = Long.parseLong(requestedRange.get(1)); // chunk smaller than file, to/from specified
    } else {
      to = from + chunkSize - 1;
    }

    long contentLength;
    if (audioOrVideo) {
      contentLength = Math.min(to - from + 1, totalBytes);
    } else {
      contentLength = totalBytes - from;
    }

    final HttpStatus status = audioOrVideo ? HttpStatus.PARTIAL_CONTENT_206 : HttpStatus.OK_200;

    ctx.header(Constants.CONTENT_RANGE, "bytes " + from + "-" + to + "/" + totalBytes);
    ctx.status(status).contentLength(contentLength);
    try (var os = ctx.outputStream()) {
      write(os, inputStream, from, to);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void write(OutputStream outputStream, InputStream inputStream, long from, long to) throws IOException {
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    long toSkip = from;
    while (toSkip > 0) {
      toSkip -= inputStream.skip(toSkip);
    }
    long bytesLeft = to - from + 1;
    while (bytesLeft > 0) {
      int read = inputStream.read(buffer, 0, (int) Math.min(DEFAULT_BUFFER_SIZE, bytesLeft));
      if (read == -1) {
        break; // End of stream reached unexpectedly
      }
      outputStream.write(buffer, 0, read);
      bytesLeft -= read;
    }
  }

  private static boolean isAudioOrVideo(String contentType) {
    return contentType.startsWith("audio/") || contentType.startsWith("video/");
  }
}
