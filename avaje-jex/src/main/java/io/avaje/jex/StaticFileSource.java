package io.avaje.jex;

public record StaticFileSource(String urlPathPrefix, String path, Location location) {

  public enum Location {
    CLASSPATH,
    EXTERNAL
  }
}
