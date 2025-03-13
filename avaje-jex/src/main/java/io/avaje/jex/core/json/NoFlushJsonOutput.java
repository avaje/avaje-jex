package io.avaje.jex.core.json;

import java.io.IOException;
import java.io.OutputStream;

import io.avaje.json.stream.JsonOutput;

final class NoFlushJsonOutput implements JsonOutput {

  private final OutputStream outputStream;

  NoFlushJsonOutput(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  @Override
  public void write(byte[] content, int offset, int length) throws IOException {
    outputStream.write(content, offset, length);
  }

  @Override
  public void flush() throws IOException {
    // no flush
  }

  @Override
  public void close() throws IOException {
    outputStream.close();
  }

  @Override
  public OutputStream unwrapOutputStream() {
    return outputStream;
  }
}
