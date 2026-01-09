package io.avaje.jex.websocket.internal;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.sun.net.httpserver.Headers;

import io.avaje.jex.http.BadRequestException;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.ExchangeHandler;
import io.avaje.jex.http.HttpResponseException;
import io.avaje.jex.http.HttpStatus;
import io.avaje.jex.http.InternalServerErrorException;

public abstract class WebSocketHandler implements ExchangeHandler {

  @Override
  public void handle(Context ctx) throws IOException {
    var headers = ctx.requestHeaders();

    if (!isWebsocketRequested(headers)) {
      throw new HttpResponseException(HttpStatus.UPGRADE_REQUIRED_426, "Not a websocket request");
    }

    if (!Util.HEADER_WEBSOCKET_VERSION_VALUE.equalsIgnoreCase(
            headers.getFirst(Util.HEADER_WEBSOCKET_VERSION))
        || !headers.containsKey(Util.HEADER_WEBSOCKET_KEY)) {
      throw new BadRequestException(
          "Invalid Websocket-Version " + headers.getFirst(Util.HEADER_WEBSOCKET_VERSION));
    }

    var webSocket = openWebSocket(ctx);

    try {
      ctx.header(
          Util.HEADER_WEBSOCKET_ACCEPT,
          Util.makeAcceptKey(headers.getFirst(Util.HEADER_WEBSOCKET_KEY)));
    } catch (NoSuchAlgorithmException e) {
      throw new InternalServerErrorException(
          "The SHA-1 Algorithm required for websockets is not available on the server.");
    }

    if (headers.containsKey(Util.HEADER_WEBSOCKET_PROTOCOL)) {
      ctx.header(
          Util.HEADER_WEBSOCKET_PROTOCOL,
          headers.getFirst(Util.HEADER_WEBSOCKET_PROTOCOL).split(",")[0]);
    }

    ctx.header(Util.HEADER_UPGRADE, Util.HEADER_UPGRADE_VALUE);
    ctx.header(Util.HEADER_CONNECTION, Util.HEADER_UPGRADE);
    ctx.writeEmpty(101);

    // this won't return until websocket is closed
    webSocket.readWebsocket();
  }

  private static boolean isWebsocketRequested(Headers headers) {
    // check if Upgrade connection
    var values = headers.get(Util.HEADER_CONNECTION);
    if (values == null
        || values.stream().filter(Util.HEADER_UPGRADE::equalsIgnoreCase).findAny().isEmpty()) {
      return false;
    }
    // check for proper upgrade type
    var upgrade = headers.getFirst(Util.HEADER_UPGRADE);
    return Util.HEADER_UPGRADE_VALUE.equalsIgnoreCase(upgrade);
  }

  protected abstract AbstractWebSocket openWebSocket(Context exchange);
}
