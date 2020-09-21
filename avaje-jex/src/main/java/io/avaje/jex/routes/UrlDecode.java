package io.avaje.jex.routes;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class UrlDecode {

  public static String decode(String s) {
    try {
      return URLDecoder.decode(s.replace("+", "%2B"), "UTF-8").replace("%2B", "+");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Not expected", e);
    }
  }

}
