package io.avaje.jex.core;

import io.avaje.jex.AppLifecycle;
import io.avaje.jex.Jex;
import io.avaje.jex.http.Context;
import io.avaje.jex.spi.JexPlugin;

/** Health plugin with liveness and readiness support based on the application lifecycle support. */
final class HealthPlugin implements JexPlugin {

  private AppLifecycle lifecycle;

  @Override
  public void apply(Jex jex) {
    lifecycle = jex.lifecycle();
    jex.routing().get("/health/liveness", this::liveness);
    jex.routing().get("/health/readiness", this::readiness);
  }

  private void readiness(Context context) {
    if (lifecycle.isReady()) {
      context.text("ok");
    } else {
      context.status(500).text("not-ready");
    }
  }

  private void liveness(Context context) {
    if (lifecycle.isAlive()) {
      context.text("ok");
    } else {
      context.status(500).text("not-alive");
    }
  }
}
