package io.avaje.jex.http2.robaho;

import io.avaje.jex.Jex;
import io.avaje.jex.spi.JexPlugin;
import robaho.net.httpserver.DefaultHttpServerProvider;

/** A plugin that configures Jex to enable HTTP/2 using Jetty. */
public final class RobahoHttp2JexPlugin implements JexPlugin {

  private RobahoHttp2JexPlugin() {}

  /**
   * Creates a new instance of the {@code RobahoHttp2JexPlugin}.
   *
   * @return The new plugin instance.
   */
  public static RobahoHttp2JexPlugin create() {
    return new RobahoHttp2JexPlugin();
  }

  @Override
  public void apply(Jex jex) {
    System.setProperty("robaho.net.httpserver.http2OverSSL", "true");
    jex.config()
        .serverProvider(new DefaultHttpServerProvider());
  }
}
