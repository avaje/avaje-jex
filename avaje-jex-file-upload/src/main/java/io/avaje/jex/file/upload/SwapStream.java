package io.avaje.jex.file.upload;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

import io.avaje.jex.http.HttpResponseException;
import io.avaje.jex.http.HttpStatus;

class SwapStream extends FilterOutputStream {

  private final ByteArrayOutputStream baos;
  private final int maxMemory;
  private final long maxFileSize;
  private boolean swapped = false;
  private long size = 0;
  private File file;

  public SwapStream(ByteArrayOutputStream baos, File file, MultipartConfig multipartConfig) {
    super(baos);
    this.baos = baos;
    this.file = file;
    this.maxFileSize = multipartConfig.maxFileSize();
    this.maxMemory = multipartConfig.maxInMemoryFileSize();
  }

  @Override
  public void write(int b) throws IOException {

    size += 1;
    if (!swapped && maxMemory > -1 && size > maxMemory) {
      swapToFileStream();
    } else if (maxFileSize > -1 && size > maxFileSize) {
      throwTooBig();
    }
    out.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {

    size += len;
    if (!swapped && maxMemory > -1 && size > maxMemory) {
      swapToFileStream();
    } else if (maxFileSize > -1 && size > maxFileSize) {
      throwTooBig();
    }
    out.write(b, off, len);
  }

  private void throwTooBig() {
    throw new HttpResponseException(
        HttpStatus.REQUEST_ENTITY_TOO_LARGE_413,
        "Uploaded file exceeds max size of %s bytes".formatted(maxFileSize));
  }

  private void swapToFileStream() throws IOException {
    out = new NoSyncBufferedOutputStream(new FileOutputStream(file));
    out.write(baos.toByteArray());
    swapped = true;
  }

  public boolean swapped() {
    return swapped;
  }

  public ByteArrayOutputStream bytes() {
    return baos;
  }
}
