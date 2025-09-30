package io.avaje.jex.core;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

final class BufferedOutStream extends FilterOutputStream {

  private final long max;
  private final JdkContext context;
  private ByteArrayOutputStream buffer;
  private boolean jdkOutput;
  private long count;

  BufferedOutStream(JdkContext context, int initial, long max) {
    super(context.exchange().getResponseBody());
    this.context = context;
    this.max = max;

    // if content length is set, skip buffer
    if (context.responseHeader(Constants.CONTENT_LENGTH) != null) {
      count = max + 1;
    } else {
      buffer = new ByteArrayOutputStream(initial);
    }
  }

  @Override
  public void write(int b) throws IOException {
    if (jdkOutput) {
      out.write(b);
    } else {
      if (count++ > max) {
        useJdkOutput();
        out.write(b);
        return;
      }
      buffer.write(b);
    }
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if (jdkOutput) {
      out.write(b, off, len);
    } else {
      count += len;
      if (count > max) {
        useJdkOutput();
        out.write(b, off, len);
        return;
      }
      buffer.write(b, off, len);
    }
  }

  /** Use the underlying OutputStream. Chunking the response if needed */
  private void useJdkOutput() throws IOException {
    // if a manual content-length is set, honor that instead of chunking
    var length = context.responseHeader(Constants.CONTENT_LENGTH);
    context.exchange().sendResponseHeaders(context.statusCode(), length == null ? 0 : Long.parseLong(length));
    jdkOutput = true;
    // empty the existing buffer
    if (buffer != null) {
      buffer.writeTo(out);
    }
  }

  @Override
  public void close() throws IOException {
    if (jdkOutput) {
      out.close();
    } else {
      context.write(buffer.toByteArray());
    }
  }
}
