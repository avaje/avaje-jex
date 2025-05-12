package io.avaje.jex.core.json;

import io.avaje.jex.http.Context;
import io.avaje.json.stream.JsonOutput;
import java.io.IOException;
import java.io.OutputStream;

/**
 * avaje-jsonb output that allows for writing fixed length content straight from the avaje-jsonb
 * buffer, avoiding the jex side buffer.
 */
public final class JsonbOutput implements JsonOutput {

  private final Context context;
  private OutputStream os;

  public static JsonOutput of(Context context) {
    return new JsonbOutput(context);
  }

  private JsonbOutput(Context context) {
    this.context = context;
  }

  @Override
  public void write(byte[] content, int offset, int length) throws IOException {
    if (os == null) {
      // exceeds the avaje-jsonb buffer size
      os = context.outputStream();
    }
    os.write(content, offset, length);
  }

  @Override
  public void writeLast(byte[] content, int offset, int length) throws IOException {
    if (os == null) {
      // write as fixed length content straight from the avaje-jsonb buffer
      context.write(content, length);
    } else {
      os.write(content, offset, length);
    }
  }

  @Override
  public void flush() {
    // shouldn't manually flush
  }

  @Override
  public void close() throws IOException {
    if (os != null) {
      os.close();
    }
  }

  @Override
  public OutputStream unwrapOutputStream() {
    return context.outputStream();
  }
}
