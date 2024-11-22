package io.avaje.jex.jdk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;

class BufferedOutStream extends OutputStream {

  private static final long MAX = Long.getLong("jex.outputBuffer.max", 1024);
  private static final int INITIAL =
      Integer.getInteger("jex.outputBuffer.initial", 256);

  private final JdkContext context;
  private ByteArrayOutputStream buffer;
  private OutputStream stream;
  private long count;

  BufferedOutStream(JdkContext context) {
    this.context = context;
    this.buffer = new ByteArrayOutputStream(INITIAL);
  }

  @Override
  public void write(int b) throws IOException {
    if (stream != null) {
      stream.write(b);
    } else {
      buffer.write(b);
      if (count++ > MAX) {
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
      if (count > MAX) {
        initialiseChunked();
      }
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
      stream.flush();
      stream.close();
    } else {
      context.writeBytes(buffer.toByteArray());
    }
  }
}
