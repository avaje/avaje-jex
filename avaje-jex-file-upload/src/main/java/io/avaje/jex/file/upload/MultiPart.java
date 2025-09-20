package io.avaje.jex.file.upload;

import java.io.File;

/**
 * a multipart part.
 *
 * <p>either data or file will be non-null, but not both.
 *
 * @param contentType the content type of the data
 * @param filename the form provided filename
 * @param file points to the uploaded file data (the name may differ from filename). This file is
 *     marked as delete on exit.
 * @param data if contains the part data as a String.
 */
public record MultiPart(String contentType, String filename, String data, File file) {}
