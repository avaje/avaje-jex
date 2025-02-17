package io.avaje.jex.core;

import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

final class BufferedOutStream extends OutputStream {

  private final long max;
  private final JdkContext context;
  private ByteArrayOutputStream buffer;
  private OutputStream stream;
  private long count;

  BufferedOutStream(JdkContext context, int initial, long max) {
    this.context = context;
    this.max = max;
    this.buffer = new ByteArrayOutputStream(initial);
  }

  @Override
  public void write(int b) throws IOException {
    if (stream != null) {
      stream.write(b);
    } else {
      if (count++ > max) {
        initialiseChunked();
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
        initialiseChunked();
        stream.write(b, off, len);
        return;
      }
      buffer.write(b, off, len);
    }
  }

  /** Use responseLength 0 and chunked response. */
  private void initialiseChunked() throws IOException {
    final HttpExchange exchange = context.exchange();
    exchange.sendResponseHeaders(context.statusCode(), 0);
    stream = exchange.getResponseBody();
    // empty the existing buffer
    buffer.writeTo(stream);
    buffer = null;
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
