package io.avaje.jex.core.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import io.avaje.jex.TemplateRender;
import io.avaje.jex.spi.JexExtension;
import io.avaje.jex.spi.JsonService;

/** Core implementation of SpiServiceManager provided to specific implementations like jetty etc. */
class CoreServiceLoader {

  private static final CoreServiceLoader INSTANCE = new CoreServiceLoader();

  private final JsonService jsonService;
  private final List<TemplateRender> renders = new ArrayList<>();

  CoreServiceLoader() {
    JsonService spiJsonService = null;

    for (var spi : ServiceLoader.load(JexExtension.class)) {

      switch (spi) {
        case JsonService s -> spiJsonService = s;
        case TemplateRender r -> renders.add(r);
      }
    }
    jsonService = spiJsonService;
  }

  public static Optional<JsonService> getJsonService() {
    return Optional.ofNullable(INSTANCE.jsonService);
  }

  public static List<TemplateRender> getRenders() {
    return INSTANCE.renders;
  }
}
