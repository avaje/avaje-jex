package io.avaje.jex.core;

import io.avaje.jex.spi.IORuntimeException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class ContextUtil {

  public static final String UTF_8 = "UTF-8";

  private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

  private static final int BUFFER_MAX = 65536;

  public static String getRequestCharset(JexHttpContext ctx) {
    final String header = ctx.req.getHeader(HeaderKeys.CONTENT_TYPE);
    if (header != null) {
      return parseCharset(header);
    }
    return UTF_8;
  }

  public static String parseCharset(String header) {
    for (String val : header.split(";")) {
      val = val.trim();
      if (val.regionMatches(true, 0, "charset", 0, "charset".length())) {
        return val.split("=")[1].trim();
      }
    }
    return "UTF-8";
  }

  public static byte[] readBody(HttpServletRequest req) {
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
      throw new IORuntimeException(e);
    }
  }

  public static void copy(InputStream in, OutputStream out, int bufferSize) throws IOException {
    byte[] buffer = new byte[bufferSize];
    int len;
    while ((len = in.read(buffer, 0, bufferSize)) > 0) {
      out.write(buffer, 0, len);
    }
  }
}
