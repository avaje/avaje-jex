package io.avaje.jex;

import java.util.List;

public sealed interface StaticFileConfig permits DefaultStaticFileConfig {

  Jex addClasspath(String path);

  Jex addClasspath(String urlPrefix, String path);

  Jex addExternal(String path);

  Jex addExternal(String urlPrefix, String path);

  List<StaticFileSource> getSources();
}
