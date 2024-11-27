package io.avaje.jex.routes;

import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;

final class UrlDecode {

  static String decode(String s) {
    return URLDecoder.decode(s.replace("+", "%2B"), UTF_8).replace("%2B", "+");
  }

}
