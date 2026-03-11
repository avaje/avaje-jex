package io.avaje.jex.staticcontent;

import java.io.OutputStream;

// Simple helper to count bytes without storing them
final class CountingOutputStream extends OutputStream {
  private long count = 0;

  @Override
  public void write(int b) {
    count++;
  }

  @Override
  public void write(byte[] b, int off, int len) {
    count += len;
  }

  public long count() {
    return count;
  }
}
