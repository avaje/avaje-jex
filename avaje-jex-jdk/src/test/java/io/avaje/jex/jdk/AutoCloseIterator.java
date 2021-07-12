package io.avaje.jex.jdk;

import java.util.Iterator;

public class AutoCloseIterator<E> implements Iterator<E>, AutoCloseable {

  private final Iterator<E> it;
  private boolean closed;

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
    closed = true;
  }

  public boolean isClosed() {
    return closed;
  }
}
