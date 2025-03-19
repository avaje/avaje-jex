package io.avaje.jex.http.sse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;

final class Emitter {
  public static final String COMMENT_PREFIX = ":";
  public static final String NEW_LINE = "\n";

  private final ReentrantLock lock = new ReentrantLock();
  private final OutputStream response;
  private boolean closed = false;

  Emitter(OutputStream outputStream) {
    this.response = outputStream;
  }

  boolean isClosed() {
    return closed;
  }

  void emit(String event, InputStream data, String id) {
    try {
      lock.lock();

      if (id != null) {
        write("id: " + id + NEW_LINE);
      }
      write("event: " + event + NEW_LINE);

      try (var reader = new BufferedReader(new InputStreamReader(data, StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          write("data: " + line + NEW_LINE);
        }
      }

      write(NEW_LINE);
      response.flush();

    } catch (final IOException ignored) {
      closed = true;
    } finally {
      lock.unlock();
    }
  }

  void emit(String comment) {
    try {
      final var lines = comment.split(NEW_LINE);
      for (final String line : lines) {
        write(COMMENT_PREFIX + " " + line + NEW_LINE);
      }
      response.flush();
    } catch (final IOException ignored) {
      closed = true;
    }
  }

  private void write(String value) throws IOException {
    response.write(value.getBytes(StandardCharsets.UTF_8));
  }
}
