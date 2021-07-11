package io.avaje.jex.jetty;

import io.avaje.jex.UploadedFile;
import io.avaje.jex.spi.ProxyServiceManager;
import io.avaje.jex.spi.SpiServiceManager;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * Jetty specific service manager.
 */
class ServiceManager extends ProxyServiceManager {

  private final MultipartUtil multipartUtil;

  ServiceManager(SpiServiceManager delegate, MultipartUtil multipartUtil) {
    super(delegate);
    this.multipartUtil = multipartUtil;
  }

  List<UploadedFile> uploadedFiles(HttpServletRequest req) {
    return multipartUtil.uploadedFiles(req);
  }

  List<UploadedFile> uploadedFiles(HttpServletRequest req, String name) {
    return multipartUtil.uploadedFiles(req, name);
  }

  Map<String, List<String>> multiPartForm(HttpServletRequest req) {
    return multipartUtil.fieldMap(req);
  }
}
