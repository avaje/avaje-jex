package io.avaje.jex.staticcontent;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * The {@code Content-Disposition} header sent with a static resource.
 *
 * <p>The filename is sanitized and encoded following RFC 6266 / RFC 5987. When the filename
 * contains non ascii characters both a legacy safe {@code filename} and an {@code filename*}
 * parameter are written.
 *
 * <pre>{@code
 * StaticContent.ofFile("downloads")
 *   .directoryIndex("index.html")
 *   .contentDisposition((ctx, filename) -> ContentDisposition.attachment(filename))
 *   .build();
 *
 * }</pre>
 *
 * @param type the disposition type
 * @param filename the sanitized filename, or null when no filename is sent
 */
public record ContentDisposition(Type type, String filename) {

  /** The disposition type. */
  public enum Type {
    /** The resource is displayed by the browser. */
    INLINE("inline"),
    /** The resource is downloaded and saved by the browser. */
    ATTACHMENT("attachment");

    private final String value;

    Type(String value) {
      this.value = value;
    }

    String value() {
      return value;
    }
  }

  private static final char[] HEX = "0123456789ABCDEF".toCharArray();

  /** Characters that need no percent encoding in a RFC 5987 ext-value. */
  private static final String ATTR_CHARS = "!#$&+-.^_`|~";

  private static final String FALLBACK_NAME = "download";

  /** Names that cannot be used as a file name on windows. */
  private static final Set<String> RESERVED_DEVICE_NAMES =
      Set.of(
          "CON", "PRN", "AUX", "NUL", "COM0", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6",
          "COM7", "COM8", "COM9", "LPT0", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7",
          "LPT8", "LPT9");

  public ContentDisposition {
    Objects.requireNonNull(type, "type is required");
    filename = filename == null ? null : sanitize(filename);
  }

  /** Return an {@code inline} disposition with the given filename. */
  public static ContentDisposition inline(String filename) {
    return new ContentDisposition(Type.INLINE, filename);
  }

  /** Return an {@code inline} disposition with no filename. */
  public static ContentDisposition inline() {
    return new ContentDisposition(Type.INLINE, null);
  }

  /** Return an {@code attachment} disposition with the given filename. */
  public static ContentDisposition attachment(String filename) {
    return new ContentDisposition(Type.ATTACHMENT, filename);
  }

  /** Return an {@code attachment} disposition with no filename. */
  public static ContentDisposition attachment() {
    return new ContentDisposition(Type.ATTACHMENT, null);
  }

  /** Return the {@code Content-Disposition} header value. */
  public String headerValue() {
    if (filename == null) {
      return type.value();
    }
    var sb =
        new StringBuilder(type.value())
            .append("; filename=\"")
            .append(quoted(filename))
            .append('"');
    if (!isAscii(filename)) {
      sb.append("; filename*=UTF-8''").append(extValue(filename));
    }
    return sb.toString();
  }

  /**
   * Strip any path components, control characters and windows device names such that the value is
   * safe to write into a header and safe for the client to save to disk.
   */
  private static String sanitize(String filename) {
    int separator = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
    var basename = separator < 0 ? filename : filename.substring(separator + 1);

    var sb = new StringBuilder(basename.length());
    for (int i = 0; i < basename.length(); i++) {
      char ch = basename.charAt(i);
      // drops CR/LF header injection along with all other control characters
      if (ch >= 0x20 && ch != 0x7f) {
        sb.append(ch);
      }
    }
    var name = sb.toString().strip();
    if (name.isEmpty() || ".".equals(name) || "..".equals(name)) {
      return FALLBACK_NAME;
    }
    return isReservedDeviceName(name) ? "_" + name : name;
  }

  private static boolean isReservedDeviceName(String name) {
    int dot = name.indexOf('.');
    var stem = dot < 0 ? name : name.substring(0, dot);
    return RESERVED_DEVICE_NAMES.contains(stem.toUpperCase(Locale.ROOT));
  }

  /** Ascii only quoted-string content, non ascii characters replaced by an underscore. */
  private static String quoted(String name) {
    var sb = new StringBuilder(name.length());
    for (int i = 0; i < name.length(); i++) {
      char ch = name.charAt(i);
      if (ch > 0x7e) {
        sb.append('_');
      } else if (ch == '"' || ch == '\\') {
        sb.append('\\').append(ch);
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  /** RFC 5987 percent encoded utf8 value. */
  private static String extValue(String name) {
    var sb = new StringBuilder(name.length() * 2);
    for (byte b : name.getBytes(StandardCharsets.UTF_8)) {
      int ch = b & 0xFF;
      if (ch >= 'A' && ch <= 'Z'
          || ch >= 'a' && ch <= 'z'
          || ch >= '0' && ch <= '9'
          || ATTR_CHARS.indexOf(ch) >= 0) {
        sb.append((char) ch);
      } else {
        sb.append('%').append(HEX[ch >> 4]).append(HEX[ch & 0xF]);
      }
    }
    return sb.toString();
  }

  private static boolean isAscii(String name) {
    for (int i = 0; i < name.length(); i++) {
      if (name.charAt(i) > 0x7e) {
        return false;
      }
    }
    return true;
  }
}
