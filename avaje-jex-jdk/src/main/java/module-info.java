import io.avaje.jex.spi.SpiStartServer;

module io.avaje.jex.jdk {

  requires transitive io.avaje.jex;
  requires java.net.http;
  requires jdk.httpserver;

  provides SpiStartServer with io.avaje.jex.jdk.JdkServerStart;
}
