package io.avaje.jex.file.upload;

import java.util.List;
import java.util.Map;

/** Provides methods for accessing uploaded files from a multipart HTTP request. */
public interface FileUploadService {

  /**
   * Retrieves the first uploaded file with the specified form field name.
   *
   * <p>This is useful for form fields that are expected to contain a single file.
   *
   * @param fileName The name of the form field associated with the uploaded file.
   * @return The {@link MultiPart} object representing the first file, or {@code null} if no file
   *     with that name is found.
   */
  MultiPart uploadedFile(String fileName);

  /**
   * Retrieves a list of all uploaded files with the specified form field name.
   *
   * @param fileName The name of the form field associated with the uploaded files.
   * @return A {@link List} of {@link MultiPart} objects, or an empty list if no files with that
   *     name are found.
   */
  List<MultiPart> uploadedFiles(String fileName);

  /**
   * Retrieves a list of all uploaded files from the request, regardless of their form field name.
   *
   * @return A {@link List} of all {@link MultiPart} objects that are files, or an empty list if no
   *     files were uploaded.
   */
  List<MultiPart> uploadedFiles();

  /**
   * Retrieves a map of all uploaded files, grouped by their form field name.
   *
   * <p>The map's key is the name of the form field, and the value is a list of {@link MultiPart}
   * objects uploaded under that field name. If the request is not a multipart request, this method
   * will return an empty map.
   *
   * @return A {@link Map} where keys are form field names and values are lists of {@link MultiPart}
   *     files. Returns an empty map for non-multipart requests.
   */
  Map<String, List<MultiPart>> uploadedFileMap();
}
