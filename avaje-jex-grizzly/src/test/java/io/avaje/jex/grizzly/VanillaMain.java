package io.avaje.jex.grizzly;

import org.glassfish.grizzly.http.server.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class VanillaMain {

  public static void main(String[] args) throws IOException, InterruptedException {

    File dir = new File(".");
    System.out.println("workingDirectory" + dir.getAbsolutePath());

    CLStaticHttpHandler clStaticHttpHandler = new CLStaticHttpHandler(VanillaMain.class.getClassLoader(), "/myres/");
    StaticHttpHandler staticHttpHandler = new StaticHttpHandler();

    final HttpServer httpServer = new HttpServerBuilder()
      .handler(clStaticHttpHandler, "cl")
      .handler(staticHttpHandler, "static")
      .handler(new MyHandler())
      .build();

    httpServer.start();
    Thread.currentThread().join();
  }

  static class MyHandler extends HttpHandler {

    @Override
    public void service(Request request, Response response) throws Exception {
      final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
      final String date = format.format(new Date(System.currentTimeMillis()));
      response.setContentType("text/plain");
      response.setContentLength(date.length());
      response.getWriter().write(date);
    }
  }
}
