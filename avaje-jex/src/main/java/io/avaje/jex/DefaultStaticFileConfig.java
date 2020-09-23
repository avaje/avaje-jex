package io.avaje.jex;

import java.util.ArrayList;
import java.util.List;

class DefaultStaticFileConfig implements StaticFileConfig {

  private final List<StaticFileSource> sources = new ArrayList<>();
  private final Jex jex;

  DefaultStaticFileConfig(Jex jex) {
    this.jex = jex;
  }

  @Override
  public Jex addClasspath(String path) {
    return addClasspath("/", path);
  }

  @Override
  public Jex addClasspath(String urlPrefix, String path) {
    sources.add(new StaticFileSource(urlPrefix, path, StaticFileSource.Location.CLASSPATH));
    return jex;
  }

  @Override
  public Jex addExternal(String path) {
    return addExternal("/", path);
  }

  @Override
  public Jex addExternal(String urlPrefix, String path) {
    sources.add(new StaticFileSource(urlPrefix, path, StaticFileSource.Location.EXTERNAL));
    return jex;
  }

  @Override
  public List<StaticFileSource> getSources() {
    return sources;
  }

}
