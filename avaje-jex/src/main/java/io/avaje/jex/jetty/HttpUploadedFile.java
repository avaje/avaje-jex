package io.avaje.jex.jetty;

import io.avaje.jex.UploadedFile;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

class HttpUploadedFile implements UploadedFile {

  private final Part part;

  HttpUploadedFile(Part part) {
    this.part = part;
  }

  @Override
  public String name() {
    return part.getName();
  }

  @Override
  public String fileName() {
    return part.getSubmittedFileName();
  }

  @Override
  public InputStream content() {
    try {
      return part.getInputStream();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public String contentType() {
    return part.getContentType();
  }

  @Override
  public long size() {
    return part.getSize();
  }


  @Override
  public String toString() {
    return "name:" + name() + " fileName:" + fileName() + " size:" + size();
  }
}
