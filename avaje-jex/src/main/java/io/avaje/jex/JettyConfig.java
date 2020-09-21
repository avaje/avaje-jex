package io.avaje.jex;

public class JettyConfig {

  private boolean sessions = true;

  private boolean security = true;

  /**
   * Set to use sessions. Defaults to true.
   */
  public void setSessions(boolean sessions) {
    this.sessions = sessions;
  }

  /**
   * Set to use security. Defaults to true.
   */
  public void setSecurity(boolean security) {
    this.security = security;
  }

  public boolean isSessions() {
    return sessions;
  }

  public boolean isSecurity() {
    return security;
  }
}
