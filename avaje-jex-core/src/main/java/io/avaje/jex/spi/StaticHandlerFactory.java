package io.avaje.jex.spi;

import io.avaje.jex.Jex;
import io.avaje.jex.StaticFileSource;

import java.util.List;

public interface StaticHandlerFactory {

  StaticHandler build(Jex jex, List<StaticFileSource> sourceList);

}
