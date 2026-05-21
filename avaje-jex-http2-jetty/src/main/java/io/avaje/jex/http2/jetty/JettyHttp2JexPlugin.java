package io.avaje.jex.http2.jetty;

import io.avaje.jex.Jex;
import io.avaje.jex.spi.JexPlugin;
import org.eclipse.jetty.http.spi.JettyHttpServerProvider;
import org.eclipse.jetty.http2.api.server.ServerSessionListener;
import org.eclipse.jetty.http2.server.RawHTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/** A plugin that configures Jex to enable HTTP/2 using Jetty. */
public final class JettyHttp2JexPlugin extends ServerSessionListener.Adapter implements JexPlugin {

  private JettyHttp2JexPlugin() {}

  /**
   * Creates a new instance of the {@code JettyHttp2JexPlugin}.
   *
   * @return The new plugin instance.
   */
  public static JettyHttp2JexPlugin create() {
    return new JettyHttp2JexPlugin();
  }

  @Override
  public void apply(Jex jex) {
    Server server = new Server();
    SslContextFactory.Server ssl = new SslContextFactory.Server();

    RawHTTP2ServerConnectionFactory http2 = new RawHTTP2ServerConnectionFactory(this);
    http2.setConnectProtocolEnabled(true);

    server.addConnector(new ServerConnector(server, ssl, http2));
    JettyHttpServerProvider.setServer(server);

    jex.config()
        .serverProvider(new JettyHttpServerProvider());
  }
}
