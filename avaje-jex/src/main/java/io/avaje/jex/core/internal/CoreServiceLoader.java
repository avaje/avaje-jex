package io.avaje.jex.core.internal;

import io.avaje.applog.AppLog;
import io.avaje.config.ConfigExtension;
import io.avaje.config.ConfigParser;
import io.avaje.config.ConfigServiceLoader;
import io.avaje.config.ConfigurationLog;
import io.avaje.config.ConfigurationPlugin;
import io.avaje.config.ConfigurationSource;
import io.avaje.config.CoreConfiguration;
import io.avaje.config.DefaultConfigurationLog;
import io.avaje.config.DefaultResourceLoader;
import io.avaje.config.ModificationEventRunner;
import io.avaje.config.Parsers;
import io.avaje.config.ResourceLoader;
import io.avaje.jex.*;
import io.avaje.jex.spi.HeaderKeys;
import io.avaje.jex.spi.JsonService;
import io.avaje.jex.spi.SpiContext;
import io.avaje.jex.spi.SpiServiceManager;

import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.lang.System.Logger.Level;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Stream;

/**
 * Core implementation of SpiServiceManager provided to specific implementations like jetty etc.
 */
class CoreServiceLoader {

  private static final ConfigServiceLoader INSTANCE = new ConfigServiceLoader();

  static ConfigServiceLoader get() {
    return INSTANCE;
  }

  private final ConfigurationLog log;
  private final ResourceLoader resourceLoader;
  private final ModificationEventRunner eventRunner;
  private final List<ConfigurationSource> sources = new ArrayList<>();
  private final List<ConfigurationPlugin> plugins = new ArrayList<>();
  private final Parsers parsers;

  ConfigServiceLoader() {
    ModificationEventRunner _eventRunner = null;
    ConfigurationLog _log = null;
    ResourceLoader _resourceLoader = null;
    List<ConfigParser> otherParsers = new ArrayList<>();

    for (var spi : ServiceLoader.load(ConfigExtension.class)) {
      if (spi instanceof ConfigurationSource) {
        sources.add((ConfigurationSource) spi);
      } else if (spi instanceof ConfigurationPlugin) {
        plugins.add((ConfigurationPlugin) spi);
      } else if (spi instanceof ConfigParser) {
        otherParsers.add((ConfigParser) spi);
      } else if (spi instanceof ConfigurationLog) {
        _log = (ConfigurationLog) spi;
      } else if (spi instanceof ResourceLoader) {
        _resourceLoader = (ResourceLoader) spi;
      } else if (spi instanceof ModificationEventRunner) {
        _eventRunner = (ModificationEventRunner) spi;
      }
    }

    this.log = _log == null ? new DefaultConfigurationLog() : _log;
    this.resourceLoader = _resourceLoader == null ? new DefaultResourceLoader() : _resourceLoader;
    this.eventRunner = _eventRunner == null ? new CoreConfiguration.ForegroundEventRunner() : _eventRunner;
    this.parsers = new Parsers(otherParsers);
  }

  Parsers parsers() {
    return parsers;
  }

  ConfigurationLog log() {
    return log;
  }

  ResourceLoader resourceLoader() {
    return resourceLoader;
  }

  ModificationEventRunner eventRunner() {
    return eventRunner;
  }

  List<ConfigurationSource> sources() {
    return sources;
  }

  List<ConfigurationPlugin> plugins() {
    return plugins;
  }
}
