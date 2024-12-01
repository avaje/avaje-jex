package io.avaje.jex.compression;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

final class GzipCompressor implements Compressor {
  static final String ENCODING = "gzip";
  private final int level;

  GzipCompressor() {
    level = 6;
  }

  GzipCompressor(int level) {
    if (level < 0 || level > 9) {
      throw new IllegalArgumentException("Valid range for parameter level is 0 to 9");
    }
    this.level = level;
  }

  @Override
  public String encoding() {
    return ENCODING;
  }

  @Override
  public OutputStream compress(OutputStream out) throws IOException {
    return new LeveledGzipStream(out, level);
  }

  private static final class LeveledGzipStream extends GZIPOutputStream {

    private LeveledGzipStream(OutputStream out, int level) throws IOException {
      super(out);
      this.def.setLevel(level);
    }
  }
}
