package io.avaje.jex.core;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoCloseIterator<E> implements Iterator<E>, AutoCloseable {

  private final Iterator<E> it;
  private final AtomicBoolean closed = new AtomicBoolean(false);

  public AutoCloseIterator(Iterator<E> it) {
    this.it = it;
  }

  @Override
  public boolean hasNext() {
    return it.hasNext();
  }

  @Override
  public E next() {
    return it.next();
  }

  @Override
  public void close() {
    closed.set(true);
  }

  public boolean isClosed() {
    return closed.get();
  }
}
