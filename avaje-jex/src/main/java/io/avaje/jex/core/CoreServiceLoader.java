package io.avaje.jex.core;

import io.avaje.jex.spi.JexExtension;
import io.avaje.jex.spi.JexPlugin;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.TemplateRender;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/** Loads SPI Services. */
final class CoreServiceLoader {

  private static final CoreServiceLoader INSTANCE = new CoreServiceLoader();

  private final JsonService jsonService;
  private final List<TemplateRender> renders = new ArrayList<>();
  private final List<JexPlugin> plugins = new ArrayList<>();

  CoreServiceLoader() {
    JsonService spiJsonService = null;
    for (var spi : ServiceLoader.load(JexExtension.class)) {
      switch (spi) {
        case JsonService s -> spiJsonService = s;
        case TemplateRender r -> renders.add(r);
        case JexPlugin p -> plugins.add(p);
      }
    }
    jsonService = spiJsonService;
  }

  static Optional<JsonService> jsonService() {
    return Optional.ofNullable(INSTANCE.jsonService);
  }

  static List<TemplateRender> getRenders() {
    return INSTANCE.renders;
  }

  static List<JexPlugin> plugins() {
    return INSTANCE.plugins;
  }
}
