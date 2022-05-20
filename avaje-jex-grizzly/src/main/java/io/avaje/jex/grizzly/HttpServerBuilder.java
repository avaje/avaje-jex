package io.avaje.jex.grizzly;

import org.glassfish.grizzly.http.server.*;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.utils.Charsets;

import java.lang.System.Logger.Level;

public class HttpServerBuilder {

  private static final System.Logger log = System.getLogger("io.avaje.jex");

  private int port = -1;
  private String host = "0.0.0.0";
  private boolean secure;
  private SSLEngineConfigurator sslEngineConfigurator;

  private final HttpServer server = new HttpServer();

  public HttpServerBuilder setPort(int port) {
    this.port = port;
    return this;
  }

  public HttpServerBuilder host(String host) {
    this.host = host;
    return this;
  }

  public HttpServerBuilder sslEngineConfigurator(SSLEngineConfigurator sslEngineConfigurator) {
    this.sslEngineConfigurator = sslEngineConfigurator;
    return this;
  }

  public HttpServerBuilder secure(boolean secure) {
    this.secure = secure;
    return this;
  }

  /**
   * Add a handler using root context.
   */
  public HttpServerBuilder handler(HttpHandler handler) {
    return handler(handler, "");
  }

  /**
   * Add a handler with the given context.
   */
  public HttpServerBuilder handler(HttpHandler handler, String context) {
    handler(handler, HttpHandlerRegistration.fromString("/" + context + "/*"));
    return this;
  }

  /**
   * Add a handler given the paths.
   */
  public HttpServerBuilder handler(HttpHandler handler, HttpHandlerRegistration... paths) {
    server.getServerConfiguration().addHttpHandler(handler, paths);
    return this;
  }

  /**
   * Build and return the grizzly http server.
   */
  public HttpServer build() {

    int serverPort = serverPort();
    NetworkListener listener = new NetworkListener("grizzly", host, serverPort);

    // TODO: Configure to use loom thread factory
    // listener.getTransport().getWorkerThreadPoolConfig().setThreadFactory()
    listener.setSecure(secure);
    if (sslEngineConfigurator != null) {
      listener.setSSLEngineConfig(sslEngineConfigurator);
    }
    addHttp2Support(listener);
    server.addListener(listener);
    ServerConfiguration config = server.getServerConfiguration();
    config.setPassTraceRequest(true);
    config.setDefaultQueryEncoding(Charsets.UTF8_CHARSET);
    return server;
  }

  protected void addHttp2Support(NetworkListener listener) {
    try {
      Class.forName("org.glassfish.grizzly.http2.Http2AddOn");
//      listener.registerAddOn(new org.glassfish.grizzly.http2.Http2AddOn());
    } catch (Throwable e) {
      log.log(Level.TRACE, "Http2AddOn was not registered");
    }
  }

  protected int serverPort() {
    return port != -1 ? port : (secure ? 8443 : 7001);
  }
}
