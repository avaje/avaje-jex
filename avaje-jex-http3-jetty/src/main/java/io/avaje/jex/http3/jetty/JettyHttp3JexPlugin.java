package io.avaje.jex.http3.jetty;

import io.avaje.applog.AppLog;
import io.avaje.jex.Jex;
import io.avaje.jex.spi.JexPlugin;
import org.eclipse.jetty.http.spi.JettyHttpServerProvider;
import org.eclipse.jetty.http3.api.Session;
import org.eclipse.jetty.http3.server.RawHTTP3ServerConnectionFactory;
import org.eclipse.jetty.quic.server.QuicServerConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.net.DatagramSocket;
import java.net.SocketAddress;

import static java.lang.System.Logger.Level.*;

/** A plugin that configures Jex to enable HTTP/3 using Jetty. */
public final class JettyHttp3JexPlugin implements JexPlugin, Session.Server.Listener {

  private static final System.Logger LOG = AppLog.getLogger(JettyHttp3JexPlugin.class);

  private JettyHttp3JexPlugin() {}

  /**
   * Creates a new instance of the {@code JettyHttp3JexPlugin}.
   *
   * @return The new plugin instance.
   */
  public static JettyHttp3JexPlugin create() {
    return new JettyHttp3JexPlugin();
  }

  @Override
  public void apply(Jex jex) {
    Server server = new Server();
    SslContextFactory.Server ssl = new SslContextFactory.Server();

    RawHTTP3ServerConnectionFactory http3 = new RawHTTP3ServerConnectionFactory(this);
    http3.getHTTP3Configuration().setStreamIdleTimeout(15000);

    server.addConnector(new QuicServerConnector(server, ssl, http3));
    JettyHttpServerProvider.setServer(server);

    jex.config()
        .serverProvider(new JettyHttpServerProvider());
  }

  @Override
  public void onAccept(Session session) {
    SocketAddress address = session.getRemoteSocketAddress();
    LOG.log(TRACE, "Connection from {0}", address);
  }
}
