package io.avaje.jex.core;

import io.avaje.jex.UploadedFile;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

class MultipartUtil {

  private final MultipartConfigElement config;

  MultipartUtil(MultipartConfigElement config) {
    this.config = config;
  }

  private void setConfig(HttpServletRequest req) {
    req.setAttribute("org.eclipse.jetty.multipartConfig", config);
  }

  List<UploadedFile> uploadedFiles(HttpServletRequest req) {
    try {
      setConfig(req);
      return req.getParts().stream()
        .filter(part -> isFile(part))
        .map(this::toUploaded)
        .collect(toList());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (ServletException e) {
      throw new RuntimeException(e);
    }
  }

  List<UploadedFile> uploadedFiles(HttpServletRequest req, String partName) {
    try {
      setConfig(req);
      return req.getParts().stream()
        .filter(part -> part.getName().equals(partName) && isFile(part))
        .map(this::toUploaded)
        .collect(toList());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (ServletException e) {
      throw new RuntimeException(e);
    }
  }

  UploadedFile toUploaded(Part part) {
    return new PartUploadedFile(part);
  }

  Map<String, List<String>> fieldMap(HttpServletRequest req) {
    setConfig(req);
    try {
      Map<String, List<String>> map = new LinkedHashMap<>();
      for (Part part : req.getParts()) {
        if (isField(part)) {
          final String name = part.getName();
          final String value = readAsString(part);
          map.computeIfAbsent(name, s -> new ArrayList<>()).add(value);
        }
      }
      return map;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (ServletException e) {
      throw new RuntimeException(e);
    }
  }

  private String readAsString(Part part) {
    try {
      return new BufferedReader(new InputStreamReader(part.getInputStream(), StandardCharsets.UTF_8))
        .lines()
        .collect(joining("\n"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static boolean isFile(Part filePart) {
    return !isField(filePart);
  }

  private static boolean isField(Part filePart) {
    return filePart.getSubmittedFileName() == null; // this is what Apache FileUpload does ...
  }

}
