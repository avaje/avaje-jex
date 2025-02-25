package io.avaje.jex.routes;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLDecoder;
import java.nio.charset.Charset;

public final class UrlDecode {

  public static String decode(String s) {
    return decode(s, UTF_8);
  }

  public static String decode(String s, Charset charset) {
    return URLDecoder.decode(s.replace("+", "%2B"), charset).replace("%2B", "+");
  }
}
