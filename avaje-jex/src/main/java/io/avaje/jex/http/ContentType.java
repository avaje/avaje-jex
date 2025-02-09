package io.avaje.jex.http;

public enum ContentType {

  TEXT_PLAIN("text/plain"),
  TEXT_CSS("text/css"),
  TEXT_CSV("text/csv"),
  TEXT_HTML("text/html"),
  TEXT_JS("text/javascript"),
  TEXT_MARKDOWN("text/markdown"),
  TEXT_PROPERTIES("text/x-java-properties"),
  TEXT_XML("text/xml"),

  IMAGE_AVIF("image/avif"),
  IMAGE_BMP("image/bmp"),
  IMAGE_GIF("image/gif"),
  IMAGE_ICO("image/vnd.microsoft.icon"),
  IMAGE_JPEG("image/jpeg"),
  IMAGE_PNG("image/png"),
  IMAGE_SVG("image/svg+xml"),
  IMAGE_TIFF("image/tiff"),
  IMAGE_WEBP("image/webp"),

  AUDIO_AAC("audio/aac"),
  AUDIO_MIDI("audio/midi"),
  AUDIO_MPEG("audio/mpeg"),
  AUDIO_OGA("audio/ogg"),
  AUDIO_OPUS("audio/opus"),
  AUDIO_WAV("audio/wav"),
  AUDIO_WEBA("audio/weba"),

  VIDEO_AVI("video/x-msvideo"),
  VIDEO_MP4("video/mp4"),
  VIDEO_MPEG("video/mpeg"),
  VIDEO_OGG("video/ogg"),
  VIDEO_WEBM("video/webm"),

  FONT_OTF("font/otf"),
  FONT_TTF("font/ttf"),
  FONT_WOFF("font/woff"),
  FONT_WOFF2("font/woff2"),

  APPLICATION_OCTET_STREAM("application/octet-stream"),
  APPLICATION_BZ("application/x-bzip"),
  APPLICATION_BZ2("application/x-bzip2"),
  APPLICATION_CDN("application/cdn"),
  APPLICATION_DOC("application/msword"),
  APPLICATION_DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
  APPLICATION_EPUB("application/epub+zip"),
  APPLICATION_GZ("application/gzip"),
  APPLICATION_JSON("application/json"),
  APPLICATION_MPKG("application/vnd.apple.installer+xml"),
  APPLICATION_JAR("application/java-archive"),
  APPLICATION_PDF("application/pdf"),
  APPLICATION_POM("application/xml"),
  APPLICATION_RAR("application/vnd.rar"),
  APPLICATION_SH("application/x-sh"),
  APPLICATION_SWF("application/x-shockwave-flash"),
  APPLICATION_TAR("application/x-tar"),
  APPLICATION_XHTML("application/xhtml+xml"),
  APPLICATION_YAML("application/yaml"),
  APPLICATION_ZIP("application/zip"),
  APPLICATION_7Z("application/x-7z-compressed"),

  MULTIPART_FORM_DATA("multipart/form-data");

  private final String content;

  ContentType(String contentType) {
    this.content = contentType;
  }

  public String contentType() {
    return content;
  }
}
