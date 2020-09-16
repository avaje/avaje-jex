package io.avaje.jex;

public class JexConfigJetty {

  boolean sessions = true;

  boolean security = true;

  public boolean isSessions() {
    return sessions;
  }

  public boolean isSecurity() {
    return security;
  }
}
