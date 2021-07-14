package io.avaje.jex.grizzly;

import org.glassfish.grizzly.http.server.Request;

import java.io.*;

class ContextUtil {

  private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

  private static final int BUFFER_MAX = 65536;

  static byte[] requestBodyAsBytes(Request req) {
    final int len = req.getContentLength();
    try (final InputStream inputStream = req.getInputStream()) {

      int bufferSize = len > -1 ? len : DEFAULT_BUFFER_SIZE;
      if (bufferSize > BUFFER_MAX) {
        bufferSize = BUFFER_MAX;
      }
      ByteArrayOutputStream os = new ByteArrayOutputStream(bufferSize);
      copy(inputStream, os, bufferSize);
      return os.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  static void copy(InputStream in, OutputStream out, int bufferSize) throws IOException {
    byte[] buffer = new byte[bufferSize];
    int len;
    while ((len = in.read(buffer, 0, bufferSize)) > 0) {
      out.write(buffer, 0, len);
    }
  }

  static String requestBodyAsString(Request request) {
    final long requestLength = request.getContentLengthLong();
    if (requestLength == 0) {
      return "";
    }
    if (requestLength < 0) {
      throw new IllegalStateException("No content-length set?");
    }
    final int bufferSize = requestLength > 512 ? 512 : (int)requestLength;

    StringWriter writer = new StringWriter((int)requestLength);
    final Reader reader = request.getReader();
    try {
      long transferred = 0;
      char[] buffer = new char[bufferSize];
      int nRead;
      while ((nRead = reader.read(buffer, 0, bufferSize)) >= 0) {
        writer.write(buffer, 0, nRead);
        transferred += nRead;
        if (transferred == requestLength) {
          break;
        }
      }
      return writer.toString();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
