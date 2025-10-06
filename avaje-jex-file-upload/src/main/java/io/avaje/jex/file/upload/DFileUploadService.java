package io.avaje.jex.file.upload;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.avaje.jex.http.Context;

final class DFileUploadService implements FileUploadService {

  private final MultipartConfig multipartConfig;
  private final Context ctx;
  private Map<String, List<MultiPart>> uploadedFilesMap;

  DFileUploadService(MultipartConfig multipartConfig, Context ctx) {
    this.multipartConfig = multipartConfig;
    this.ctx = ctx;
  }

  private void ensureParsed() {
    if (uploadedFilesMap == null) {
      var contentType = ctx.contentType();
      if (contentType == null || !contentType.startsWith("multipart/form-data")) {
        uploadedFilesMap = Map.of();
        return;
      }
      try {
        uploadedFilesMap = MultipartFormParser.parse(charset(), contentType, ctx, multipartConfig);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  @Override
  public MultiPart uploadedFile(String fileName) {
    ensureParsed();
    var files = uploadedFilesMap.get(fileName);
    return files != null && !files.isEmpty() ? files.get(0) : null;
  }

  @Override
  public List<MultiPart> uploadedFiles(String fileName) {
    ensureParsed();
    var files = uploadedFilesMap.get(fileName);
    return files != null ? files : java.util.Collections.emptyList();
  }

  @Override
  public List<MultiPart> uploadedFiles() {
    ensureParsed();
    List<MultiPart> all = new ArrayList<>();
    for (List<MultiPart> parts : uploadedFilesMap.values()) {
      all.addAll(parts);
    }
    return all;
  }

  @Override
  public Map<String, List<MultiPart>> uploadedFileMap() {
    ensureParsed();
    return uploadedFilesMap;
  }

  private Charset charset() {
    return parseCharset(ctx.header("Content-type"));
  }

  private static Charset parseCharset(String header) {
    if (header != null) {
      for (String val : header.split(";")) {
        val = val.trim();
        if (val.regionMatches(true, 0, "charset", 0, "charset".length())) {
          return Charset.forName(val.split("=")[1].trim());
        }
      }
    }
    return StandardCharsets.ISO_8859_1;
  }
}
