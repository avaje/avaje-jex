package io.avaje.jex;

import java.io.InputStream;

/**
 * An uploaded file.
 */
public interface UploadedFile {

  /**
   * Return the name of the part.
   */
  String name();

  /**
   * Return the submitted file name.
   */
  String fileName();

  /**
   * Return the file content as InputStream.
   */
  InputStream content();

  /**
   * Return the content type for this part.
   */
  String contentType();

  /**
   * Return the size.
   */
  long size();

}
