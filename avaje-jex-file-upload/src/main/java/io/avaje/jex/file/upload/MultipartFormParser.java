package io.avaje.jex.file.upload;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.avaje.jex.http.BadRequestException;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.HttpResponseException;
import io.avaje.jex.http.HttpStatus;

/** parse multipart form data */
class MultipartFormParser {
  private record PartMetadata(String contentType, String name, String filename) {}

  /**
   * parse a multi-part input stream, write files to storage. The caller is responsible to delete
   * files when they are no longer needed.
   *
   * @return a map of key to either a String (non-file) or a File
   */
  static Map<String, List<MultiPart>> parse(
      Charset charset, String contentType, Context ctx, MultipartConfig config) throws IOException {

    if (!contentType.contains("boundary=")) {
      throw new BadRequestException("content type does not contain boundary");
    } else if (config.maxRequestSize() > -1 && ctx.contentLength() > config.maxRequestSize()) {
      throw new HttpResponseException(
          HttpStatus.REQUEST_ENTITY_TOO_LARGE_413,
          "Request exceeds max size of %s bytes".formatted(config.maxRequestSize()));
    }

    var boundary = contentType.split("boundary=")[1];

    var is = new BufferedInputStream(ctx.bodyAsInputStream());

    Map<String, List<MultiPart>> results = new HashMap<>();

    // the CRLF is considered part of the boundary
    var boundaryCheck = ("\r\n--" + boundary).getBytes(charset);

    List<String> headers = new ArrayList<>();

    // read until boundary found
    int matchCount =
        2; // starting at 2 allows matching non-compliant senders. rfc says CRLF is part of
    // boundary marker
    while (true) {
      int c = is.read();
      if (c == -1) {
        return results;
      }
      if (c == boundaryCheck[matchCount]) {
        matchCount++;
        if (matchCount == boundaryCheck.length - 2) {
          break;
        }
      } else {
        matchCount = 0;
        if (c == boundaryCheck[matchCount]) {
          matchCount++;
        }
      }
    }

    // read to end of line
    String s = readLine(charset, is);
    if (s == null || "--".equals(s)) {
      return results;
    }

    headers.clear();

    while (true) {
      // read part headers until blank line
      while (true) {
        s = readLine(charset, is);
        if (s == null) {
          return results;
        }
        if ("".equals(s)) {
          break;
        }
        headers.add(s);
      }

      // read part data - need to detect end of part
      var meta = parseHeaders(headers);
      var fileName = meta.filename != null ? meta.filename : meta.name + ".tmp";
      var file = Path.of(config.cacheDirectory(), fileName).toFile();
      file.deleteOnExit();

      SwapStream os = new SwapStream(new ByteArrayOutputStream(), file, config);

      try (os) {
        matchCount = 0;
        while (true) {
          int c = is.read();
          if (c == -1) {
            return results;
          }
          if (c == boundaryCheck[matchCount]) {
            matchCount++;
            if (matchCount == boundaryCheck.length) {
              break;
            }
          } else {
            if (matchCount > 0) {
              os.write(boundaryCheck, 0, matchCount);
              matchCount = 0;
            }
            if (c == boundaryCheck[matchCount]) {
              matchCount++;
            } else {
              os.write(c);
            }
          }
        }
      }

      if (!os.swapped()) {
        results
            .computeIfAbsent(meta.name, k -> new ArrayList<MultiPart>())
            .add(new MultiPart(meta.contentType, null, os.bytes().toString(charset), null));
      } else {
        results
            .computeIfAbsent(meta.name, k -> new ArrayList<MultiPart>())
            .add(new MultiPart(meta.contentType, fileName, null, file));
      }

      // read to end of line
      s = readLine(charset, is);
      if ("--".equals(s)) {
        return results;
      }
    }
  }

  private static final Pattern optionPattern = Pattern.compile("\\s(?<key>.*)=\"(?<value>.*)\"");

  private static PartMetadata parseHeaders(List<String> headers) {
    String name = null;
    String filename = null;
    String contentType = null;
    for (var header : headers) {
      String[] parts = header.split(":", 2);
      if ("content-disposition".equalsIgnoreCase(parts[0])) {
        String[] options = parts[1].split(";");
        for (var option : options) {
          Matcher m = optionPattern.matcher(option);
          if (m.matches()) {
            String key = m.group("key");
            String value = m.group("value");
            if ("name".equals(key)) {
              name = value;
            }
            if ("filename".equals(key)) {
              filename = value;
            }
          }
        }
      } else if ("content-type".equalsIgnoreCase(parts[0])) {
        contentType = parts[1];
      }
    }
    return new PartMetadata(contentType, name, filename);
  }

  private static String readLine(Charset charset, InputStream is) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    boolean prevCR = false;
    while (true) {
      int c = is.read();
      switch (c) {
        case -1:
          if (bos.size() > 0) {
            return bos.toString(charset);
          }
          return null;
        case '\r':
          prevCR = true;
          break;
        case '\n':
          if (prevCR) {
            return bos.toString(charset);
          } else {
            bos.write(c);
          }
          break;
        default:
          if (prevCR) {
            bos.write('\r');
            prevCR = false;
          }
          bos.write(c);
          break;
      }
    }
  }
}
