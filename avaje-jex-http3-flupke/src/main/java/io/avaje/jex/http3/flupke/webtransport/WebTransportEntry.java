package io.avaje.jex.http3.flupke.webtransport;

import java.util.function.Consumer;

import io.avaje.jex.http3.flupke.webtransport.WebTransportEvent.BiStream;
import io.avaje.jex.http3.flupke.webtransport.WebTransportEvent.Close;
import io.avaje.jex.http3.flupke.webtransport.WebTransportEvent.UniStream;
import tech.kwik.flupke.webtransport.Session;

/** Entry for webtransport */
public final record WebTransportEntry(String path, WebTransportHandler handler)
    implements Consumer<Session> {

  @Override
  public void accept(Session session) {

    session.registerSessionTerminatedEventListener(
        (l, s) -> handler.onClose(new Close(session, l, s)));
    session.setUnidirectionalStreamReceiveHandler(
        s -> {
          var ctx = new UniStream(session, s);
          handler.onUniDirectionalStream(ctx);
        });
    session.setBidirectionalStreamReceiveHandler(
        s -> {
          var ctx = new BiStream(session, s);
          handler.onBiDirectionalStream(ctx);
        });
    session.open();
  }
}
