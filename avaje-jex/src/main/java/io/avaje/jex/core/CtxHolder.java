package io.avaje.jex.core;
import java.util.Optional;

import io.avaje.jex.http.Context;

public final class CtxHolder {
  private static final ThreadLocal<Context> CTX = new ThreadLocal<>();

  private CtxHolder() {}

  static void runWith(Context ctx, Runnable task) {
    CTX.set(ctx);
    try {
      task.run();
    } finally {
      CTX.remove();
    }
  }

  public static Context ctx() {
    return Optional.ofNullable(CTX.get()).orElseThrow();
  }
}
