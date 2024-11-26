package io.avaje.jex.htmx;

import io.avaje.jex.ExchangeHandler;

final class DHxHandlerBuilder implements HxHandler.Builder {

  private final ExchangeHandler delegate;
  private String target;
  private String trigger;
  private String triggerName;

  DHxHandlerBuilder(ExchangeHandler delegate) {
    this.delegate = delegate;
  }

  @Override
  public DHxHandlerBuilder target(String target) {
    this.target = target;
    return this;
  }

  @Override
  public DHxHandlerBuilder trigger(String trigger) {
    this.trigger = trigger;
    return this;
  }

  @Override
  public DHxHandlerBuilder triggerName(String triggerName) {
    this.triggerName = triggerName;
    return this;
  }

  @Override
  public ExchangeHandler build() {
    return new DHxHandler(delegate, target, trigger, triggerName);
  }
}
