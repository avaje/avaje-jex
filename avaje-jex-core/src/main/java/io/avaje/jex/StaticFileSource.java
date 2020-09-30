package io.avaje.jex;

import java.util.Objects;

public class StaticFileSource {

  public enum Location {
    CLASSPATH, EXTERNAL
  }

  private final String urlPathPrefix;
  private final String path;
  private final Location location;

  public StaticFileSource(String urlPathPrefix, String path, Location location) {
    this.urlPathPrefix = urlPathPrefix;
    this.path = path;
    this.location = location;
  }

  public String getUrlPathPrefix() {
    return urlPathPrefix;
  }

  public String getPath() {
    return path;
  }

  public Location getLocation() {
    return location;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StaticFileSource that = (StaticFileSource) o;
    return urlPathPrefix.equals(that.urlPathPrefix) &&
      path.equals(that.path) &&
      location == that.location;
  }

  @Override
  public int hashCode() {
    return Objects.hash(urlPathPrefix, path, location);
  }

  @Override
  public String toString() {
    return "urlPathPrefix: " + urlPathPrefix
      + ", path: " + path
      + ", location: " + location;
  }

}
