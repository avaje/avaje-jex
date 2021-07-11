package io.avaje.jex.jdk;

import com.sun.net.httpserver.HttpServer;
import io.avaje.jex.Jex;

class JdkServer implements Jex.Server {

  private final HttpServer server;

  JdkServer(HttpServer server) {
    this.server = server;
  }

  @Override
  public void shutdown() {
    server.stop(0);
  }
}
