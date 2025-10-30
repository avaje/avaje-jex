package io.avaje.jex.core;

import io.avaje.jex.http.Context;

public class SVHolder {

  private static final ScopedValue<Context> SV = ScopedValue.newInstance();

  public static void runWith(Context ctx, Runnable task) {
    ScopedValue.where(SV, ctx).run(task);
  }

  public static Context ctx() {
    return SV.get();
  }
}
