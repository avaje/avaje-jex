package io.avaje.jex.jdk;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Parse cookies based on RFC6265 skipping parameters.
 */
class CookieParser {

  private static final String QUOTE = "\"";
  private static final char[] QUOTE_CHARS = QUOTE.toCharArray();

  private CookieParser() {
  }

  private static final String RFC2965_VERSION = "$Version";
  private static final String RFC2965_PATH = "$Path";
  private static final String RFC2965_DOMAIN = "$Domain";
  private static final String RFC2965_PORT = "$Port";

  /**
   * Parse cookies based on RFC6265 skipping parameters.
   *
   * <p>This does not support cookies with multiple values.
   *
   * @param rawHeader a value of '{@code Cookie:}' header.
   */
  public static Map<String, String> parse(String rawHeader) {
    if (rawHeader == null) {
      return emptyMap();
    }
    rawHeader = rawHeader.trim();
    if (rawHeader.isEmpty()) {
      return emptyMap();
    }

    // Beware RFC2965
    boolean isRfc2965 = false;
    if (rawHeader.regionMatches(true, 0, RFC2965_VERSION, 0, RFC2965_VERSION.length())) {
      isRfc2965 = true;
      int ind = rawHeader.indexOf(';');
      if (ind < 0) {
        return emptyMap();
      } else {
        rawHeader = rawHeader.substring(ind + 1);
      }
    }

    Map<String, String> map = new LinkedHashMap<>();
    for (String baseToken : tokenize(',', rawHeader)) {
      for (String token : tokenize(';', baseToken)) {
        int eqInd = token.indexOf('=');
        if (eqInd > 0) {
          String name = token.substring(0, eqInd).trim();
          if (name.isEmpty() || (isRfc2965 && name.charAt(0) == '$' && ignore(name))) {
            continue; // Skip RFC2965 attributes
          }
          final String value = unwrap(token.substring(eqInd + 1).trim());
          if (!value.isEmpty()) {
            map.put(name, value);
          }
        }
      }
    }
    return map;
  }

  private static boolean ignore(String name) {
    return (RFC2965_PATH.equalsIgnoreCase(name) || RFC2965_DOMAIN.equalsIgnoreCase(name)
      || RFC2965_PORT.equalsIgnoreCase(name) || RFC2965_VERSION.equalsIgnoreCase(name));
  }


  /**
   * Unwrap double-quotes if present.
   */
  private static String unwrap(String value) {
    if (value.length() >= 2 && '"' == value.charAt(0) && '"' == value.charAt(value.length() - 1)) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }

  /**
   * Tokenize with quoted sub-sequences.
   */
  static List<String> tokenize(char separator, String text) {
    StringBuilder token = new StringBuilder();
    List<String> result = new ArrayList<>();
    boolean quoted = false;
    char lastQuoteCharacter = ' ';
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (quoted) {
        if (ch == lastQuoteCharacter) {
          quoted = false;
        }
        token.append(ch);
      } else if (ch == separator) {
        if (token.length() > 0) {
          result.add(token.toString());
        }
        token.setLength(0);
      } else {
        for (char quote : CookieParser.QUOTE_CHARS) {
          if (ch == quote) {
            quoted = true;
            lastQuoteCharacter = ch;
            break;
          }
        }
        token.append(ch);
      }
    }
    if (token.length() > 0) {
      result.add(token.toString());
    }
    return result;
  }
}
