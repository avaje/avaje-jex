import io.avaje.jex.jdk.JdkServerStart;
import io.avaje.jex.spi.SpiStartServer;

module io.avaje.jex.jdk {

  requires transitive io.avaje.jex;
  requires transitive java.net.http;
  requires transitive jdk.httpserver;
  requires transitive org.slf4j;


  provides SpiStartServer with JdkServerStart;
}
