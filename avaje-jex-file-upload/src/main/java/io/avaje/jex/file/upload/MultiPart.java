package io.avaje.jex.file.upload;

import java.io.File;
import java.nio.file.Files;

/**
 * A multipart part. Closing deletes the uploaded file
 *
 * <p>either data or file will be non-null, but not both.
 *
 * @param contentType the content type of the data
 * @param filename the form provided filename
 * @param file points to the uploaded file data (the name may differ from filename). This file is
 *     marked as delete on exit.
 * @param data if contains the part data as a String.
 */
public record MultiPart(String contentType, String filename, String data, File file)
    implements AutoCloseable {

  /** Delete the file */
  @Override
  public void close() throws Exception {
    if (file != null) {
      Files.delete(file.toPath());
    }
  }
}
