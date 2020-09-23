package io.avaje.jex;

import java.util.List;

public interface StaticFileConfig {

  Jex addClasspath(String path);

  Jex addClasspath(String urlPrefix, String path);

  Jex addExternal(String path);

  Jex addExternal(String urlPrefix, String path);

  List<StaticFileSource> getSources();
}
