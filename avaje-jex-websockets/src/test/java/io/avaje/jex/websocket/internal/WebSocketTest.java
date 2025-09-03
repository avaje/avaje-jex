package io.avaje.jex.websocket.internal;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.avaje.jex.Jex;
import io.avaje.jex.test.TestPair;
import io.avaje.jex.websocket.WebSocketPlugin;

public class WebSocketTest {

  static {
    // System.setProperty("jdk.httpclient.HttpClient.log", "all");
    // System.setProperty("jdk.internal.httpclient.websocket.debug", "true");
  }

  private static final String path = "/ws";

  TestPair server;

  @BeforeEach
  public void setUp() throws IOException {

    var jex = Jex.create();

    WebSocketPlugin p = WebSocketPlugin.create();
    p.ws(path, new EchoWebSocketHandler());
    jex.plugin(p);
    server = TestPair.create(jex);

    Logger logger = Logger.getLogger(WebSocketTest.class.getName());
    ConsoleHandler ch = new ConsoleHandler();
    logger.setLevel(Level.ALL);
    ch.setLevel(Level.ALL);
    logger.addHandler(ch);
  }

  @AfterEach
  public void tearDown() {
    server.shutdown();
  }

  @Test
  public void testEcho() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    var client =
        WebSocketClientUtil.createWSC(
            server.port(),
            path,
            s -> {
              if ("a_message".equals(s)) {
                latch.countDown();
              } else {
                fail("received wrong message");
              }
            },
            null);

    client.sendText("a_message", true);

    if (!latch.await(5, TimeUnit.SECONDS)) {
      fail("did not receive message");
    }
    System.err.println("closing client");
  }
}
