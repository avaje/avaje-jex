package io.avaje.jex.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

import io.avaje.jex.spi.JexExtension;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.StaticResourceLoader;
import io.avaje.jex.spi.TemplateRender;

/** Core implementation of SpiServiceManager provided to specific implementations like jetty etc. */
public final class CoreServiceLoader {

  private static final CoreServiceLoader INSTANCE = new CoreServiceLoader();

  private final JsonService jsonService;
  private final List<TemplateRender> renders = new ArrayList<>();

  private final StaticResourceLoader resourceloader;

  CoreServiceLoader() {
    JsonService spiJsonService = null;
    StaticResourceLoader spiResourceloder = null;

    for (var spi : ServiceLoader.load(JexExtension.class)) {

      switch (spi) {
        case JsonService s -> spiJsonService = s;
        case TemplateRender r -> renders.add(r);
        case StaticResourceLoader l -> spiResourceloder = l;
      }
    }
    jsonService = spiJsonService;
    resourceloader = Objects.requireNonNullElseGet(spiResourceloder, DefaultResourceLoader::new);
  }

  public static Optional<JsonService> jsonService() {
    return Optional.ofNullable(INSTANCE.jsonService);
  }

  public static List<TemplateRender> getRenders() {
    return INSTANCE.renders;
  }

  public static StaticResourceLoader resourceLoader() {
    return INSTANCE.resourceloader;
  }
}
