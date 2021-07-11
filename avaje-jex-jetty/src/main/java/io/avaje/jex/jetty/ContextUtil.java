package io.avaje.jex.jetty;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

import java.io.*;

class ContextUtil {

  private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

  private static final int BUFFER_MAX = 65536;

  static byte[] readBody(HttpServletRequest req) {
    try {
      final ServletInputStream inputStream = req.getInputStream();

      int bufferSize = inputStream.available();
      if (bufferSize < DEFAULT_BUFFER_SIZE) {
        bufferSize = DEFAULT_BUFFER_SIZE;
      } else if (bufferSize > BUFFER_MAX) {
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

}
