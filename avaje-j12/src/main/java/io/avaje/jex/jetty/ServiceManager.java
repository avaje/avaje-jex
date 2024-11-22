package io.avaje.jex.jetty;

//import io.avaje.jex.UploadedFile;
//import io.avaje.jex.spi.ProxyServiceManager;
//import io.avaje.jex.spi.SpiServiceManager;
//import jakarta.servlet.http.HttpServletRequest;
//
//import java.util.List;
//import java.util.Map;

import io.avaje.jex.spi.ProxyServiceManager;
import io.avaje.jex.spi.SpiServiceManager;

/**
 * Jetty specific service manager.
 */
class ServiceManager extends ProxyServiceManager {
  public ServiceManager(SpiServiceManager delegate) {
    super(delegate);
  }

//  private final MultipartUtil multipartUtil;
//
//  ServiceManager(SpiServiceManager delegate, MultipartUtil multipartUtil) {
//    super(delegate);
//    this.multipartUtil = multipartUtil;
//  }
//
//  List<UploadedFile> uploadedFiles(HttpServletRequest req) {
//    return null;// multipartUtil.uploadedFiles(req);
//  }
//
//  List<UploadedFile> uploadedFiles(HttpServletRequest req, String name) {
//    return null; //multipartUtil.uploadedFiles(req, name);
//  }

//  Map<String, List<String>> multiPartForm(HttpServletRequest req) {
//    return multipartUtil.fieldMap(req);
//  }
}
