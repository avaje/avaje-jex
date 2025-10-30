package io.avaje.jex.core;

import io.avaje.jex.http.Context;

public class SVHolder {

  public static void runWith(Context __, Runnable task) {
    task.run();
  }

  public static Context ctx() {
    throw new UnsupportedOperationException("Static context only available on JDK 25+");
  }
}
