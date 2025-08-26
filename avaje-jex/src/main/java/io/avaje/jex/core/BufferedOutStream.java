package io.avaje.jex.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;

final class BufferedOutStream extends OutputStream {

  private final long max;
  private final JdkContext context;
  private ByteArrayOutputStream buffer;
  private OutputStream stream;
  private long count;

  BufferedOutStream(JdkContext context, int initial, long max) {
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
    if (stream != null) {
      stream.write(b);
    } else {
      if (count++ > max) {
        useJdkOutput();
        stream.write(b);
        return;
      }
      buffer.write(b);
    }
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if (stream != null) {
      stream.write(b, off, len);
    } else {
      count += len;
      if (count > max) {
        useJdkOutput();
        stream.write(b, off, len);
        return;
      }
      buffer.write(b, off, len);
    }
  }

  /** Use the underlying HttpExchange. Chunking the response if needed */
  private void useJdkOutput() throws IOException {
    final HttpExchange exchange = context.exchange();
    // if a manual content-length is set, honor that instead of chunking
    String length = context.responseHeader(Constants.CONTENT_LENGTH);
    exchange.sendResponseHeaders(context.statusCode(), length == null ? 0 : Long.parseLong(length));
    stream = exchange.getResponseBody();
    // empty the existing buffer
    if (buffer != null) {
      buffer.writeTo(stream);
      buffer = null;
    }
  }

  @Override
  public void close() throws IOException {
    if (stream != null) {
      stream.close();
    } else {
      context.write(buffer.toByteArray());
    }
  }
}
