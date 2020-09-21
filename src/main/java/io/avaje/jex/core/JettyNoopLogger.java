package io.avaje.jex.core;

import org.eclipse.jetty.util.log.Logger;

final class JettyNoopLogger implements Logger {

  public String getName() {
    return "noop";
  }

  public Logger getLogger(String s) {
    return this;
  }

  public void warn(String s, Object... objects) {

  }

  public void warn(Throwable throwable) {

  }

  public void warn(String s, Throwable throwable) {

  }

  public void info(String s, Object... objects) {

  }

  public void info(Throwable throwable) {

  }

  public void info(String s, Throwable throwable) {

  }

  public boolean isDebugEnabled() {
    return false;
  }

  public void setDebugEnabled(boolean b) {

  }

  public void debug(String s, Object... objects) {

  }

  public void debug(String s, long l) {

  }

  public void debug(Throwable throwable) {

  }

  public void debug(String s, Throwable throwable) {

  }

  public void ignore(Throwable throwable) {

  }
}
