package io.avaje.jex.jdk;

import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

class BufferedOutStream extends OutputStream {

  private final JdkContext context;
  private final long max;
  private ByteArrayOutputStream buffer;
  private OutputStream stream;
  private long count;

  BufferedOutStream(JdkContext context, long max, int bufferSize) {
    this.context = context;
    this.max = max;
    this.buffer = new ByteArrayOutputStream(bufferSize);
  }

  @Override
  public void write(int b) throws IOException {
    if (stream != null) {
      stream.write(b);
    } else {
      buffer.write(b);
      if (count++ > max) {
        initialiseChunked();
      }
    }
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if (stream != null) {
      stream.write(b, off, len);
    } else {
      count += len;
      buffer.write(b, off, len);
      if (count > max) {
        initialiseChunked();
      }
    }
  }

  /**
   * Use responseLength 0 and chunked response.
   */
  private void initialiseChunked() throws IOException {
    final HttpExchange exchange = context.exchange();
    exchange.sendResponseHeaders(context.statusCode(), 0);
    stream = exchange.getResponseBody();
    // empty the existing buffer
    stream.write(buffer.toByteArray());
    buffer = null;
  }

  @Override
  public void close() throws IOException {
    if (stream != null) {
      stream.flush();
      stream.close();
    } else {
      context.writeBytes(buffer.toByteArray());
    }
  }
}
