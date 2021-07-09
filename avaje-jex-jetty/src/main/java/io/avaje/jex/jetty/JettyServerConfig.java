package io.avaje.jex.jetty;

import io.avaje.jex.ServerConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class JettyServerConfig implements ServerConfig {

  private boolean sessions = true;
  private boolean security = true;
  /**
   * Set true to use Loom virtual threads for ThreadPool.
   * This requires JDK 17 with Loom included.
   */
  private boolean virtualThreads;
  /**
   * Set maxThreads when using default QueuedThreadPool. Defaults to 200.
   */
  private int maxThreads;
  private SessionHandler sessionHandler;
  private ServletContextHandler contextHandler;
  private org.eclipse.jetty.server.Server server;

  public boolean sessions() {
    return sessions;
  }

  public JettyServerConfig sessions(boolean sessions) {
    this.sessions = sessions;
    return this;
  }

  public boolean security() {
    return security;
  }

  public JettyServerConfig security(boolean security) {
    this.security = security;
    return this;
  }

  public boolean virtualThreads() {
    return virtualThreads;
  }

  public JettyServerConfig virtualThreads(boolean virtualThreads) {
    this.virtualThreads = virtualThreads;
    return this;
  }

  public int maxThreads() {
    return maxThreads;
  }

  public JettyServerConfig maxThreads(int maxThreads) {
    this.maxThreads = maxThreads;
    return this;
  }

  public SessionHandler sessionHandler() {
    return sessionHandler;
  }

  public JettyServerConfig sessionHandler(SessionHandler sessionHandler) {
    this.sessionHandler = sessionHandler;
    return this;
  }

  public ServletContextHandler contextHandler() {
    return contextHandler;
  }

  public JettyServerConfig contextHandler(ServletContextHandler contextHandler) {
    this.contextHandler = contextHandler;
    return this;
  }

  public Server server() {
    return server;
  }

  public JettyServerConfig server(Server server) {
    this.server = server;
    return this;
  }
}
