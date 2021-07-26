package org.example;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class WiredController implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(WiredController.class);

  @Produces("text/plain")
  @Get
  String hello() {
    return "Hello from Controller";
  }

  @Override
  public void close() {
    log.info("close starting ... ");
    try {
      Thread.sleep(500);
      log.info("close done");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      e.printStackTrace();
    }
  }


}
