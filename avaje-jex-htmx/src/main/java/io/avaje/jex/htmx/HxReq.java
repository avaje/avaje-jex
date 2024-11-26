package io.avaje.jex.htmx;

import io.avaje.htmx.api.HtmxRequest;
import io.avaje.jex.Context;

/**
 * Obtain the HtmxRequest for the given Jex Context.
 */
public final class HxReq {

  /**
   * Create given the server request.
   */
  public static HtmxRequest of(Context ctx) {
    String header = ctx.header(HxHeaders.HX_REQUEST);
    if (header == null) {
      return HtmxRequest.EMPTY;
    }

    var builder = HtmxRequest.builder();
    if (ctx.header(HxHeaders.HX_BOOSTED) != null) {
      builder.boosted(true);
    }
    if (ctx.header(HxHeaders.HX_HISTORY_RESTORE_REQUEST) != null) {
      builder.historyRestoreRequest(true);
    }
    var currentUrl = ctx.header(HxHeaders.HX_CURRENT_URL);
    if (currentUrl != null) {
      builder.currentUrl(currentUrl);
    }
    var prompt = ctx.header(HxHeaders.HX_PROMPT);
    if (prompt != null) {
      builder.promptResponse(prompt);
    }
    var target = ctx.header(HxHeaders.HX_TARGET);
    if (target != null) {
      builder.target(target);
    }
    var triggerName = ctx.header(HxHeaders.HX_TRIGGER_NAME);
    if (triggerName != null) {
      builder.triggerName(triggerName);
    }
    var trigger = ctx.header(HxHeaders.HX_TRIGGER);
    if (trigger != null) {
      builder.triggerId(trigger);
    }
    return builder.build();
  }
}
