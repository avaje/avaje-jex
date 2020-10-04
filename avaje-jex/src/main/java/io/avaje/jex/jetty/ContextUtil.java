package io.avaje.jex.jetty;

import io.avaje.jex.core.HeaderKeys;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class ContextUtil {

  public static final String UTF_8 = "UTF-8";

  private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

  private static final int BUFFER_MAX = 65536;

  static String getRequestCharset(JexHttpContext ctx) {
    final String header = ctx.req.getHeader(HeaderKeys.CONTENT_TYPE);
    if (header != null) {
      return parseCharset(header);
    }
    return UTF_8;
  }

  static String parseCharset(String header) {
    for (String val : header.split(";")) {
      val = val.trim();
      if (val.regionMatches(true, 0, "charset", 0, "charset".length())) {
        return val.split("=")[1].trim();
      }
    }
    return "UTF-8";
  }

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

  static Map<String, List<String>> formParamMap(String body, String charset) {
    if (body.isEmpty()) {
      return Collections.emptyMap();
    }
    try {
      Map<String, List<String>> map = new LinkedHashMap<>();
      for (String pair : body.split("&")) {
        final String[] split1 = pair.split("=", 2);
        String key = URLDecoder.decode(split1[0], charset);
        String val = split1.length > 1 ? URLDecoder.decode(split1[1], charset) : "";
        map.computeIfAbsent(key, s -> new ArrayList<>()).add(val);
      }
      return map;
    } catch (UnsupportedEncodingException e) {
      throw new UncheckedIOException(e);
    }
  }
}
