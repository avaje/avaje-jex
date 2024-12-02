package io.avaje.jex.htmx;

import static io.avaje.jex.htmx.HxHeaders.HX_REQUEST;
import static io.avaje.jex.htmx.HxHeaders.HX_TARGET;
import static io.avaje.jex.htmx.HxHeaders.HX_TRIGGER;
import static io.avaje.jex.htmx.HxHeaders.HX_TRIGGER_NAME;

import io.avaje.jex.Context;
import io.avaje.jex.ExchangeHandler;

final class DHxHandler implements ExchangeHandler {

  private final ExchangeHandler delegate;
  private final String target;
  private final String trigger;
  private final String triggerName;

  DHxHandler(ExchangeHandler delegate, String target, String trigger, String triggerName) {
    this.delegate = delegate;
    this.target = target;
    this.trigger = trigger;
    this.triggerName = triggerName;
  }

  @Override
  public void handle(Context ctx) throws Exception {
    if (ctx.header(HX_REQUEST) != null && matched(ctx)) {
      delegate.handle(ctx);
    }
  }

  private boolean matched(Context ctx) {
    if ((target != null && notMatched(ctx.header(HX_TARGET), target)) || (trigger != null && notMatched(ctx.header(HX_TRIGGER), trigger))) {
      return false;
    }
    return triggerName == null || !notMatched(ctx.header(HX_TRIGGER_NAME), triggerName);
  }

  private boolean notMatched(String header, String matchValue) {
    return header == null || !matchValue.equals(header);
  }

}
