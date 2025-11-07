package io.avaje.jex.http3.flupke.webtransport;

import java.util.function.Consumer;

import io.avaje.jex.http3.flupke.webtransport.WtContext.BiStream;
import io.avaje.jex.http3.flupke.webtransport.WtContext.UniStream;
import io.avaje.jex.http3.flupke.webtransport.WtContext.WtClose;
import io.avaje.jex.http3.flupke.webtransport.WtContext.WtError;
import io.avaje.jex.http3.flupke.webtransport.WtContext.WtStream;
import tech.kwik.flupke.webtransport.Session;

public final record WebTransportEntry(String path, WebTransportHandler handler)
    implements Consumer<Session> {

  @Override
  public void accept(Session session) {

    session.registerSessionTerminatedEventListener(
        (l, s) -> handler.onClose(new WtClose(session, l, s)));

    session.setUnidirectionalStreamReceiveHandler(
        s -> {
          var ctx = new UniStream(session, s);
          try {
            handler.onUniDirectionalStream(ctx);
          } catch (Exception e) {
            error(session, ctx, e);
          }
        });
    session.setBidirectionalStreamReceiveHandler(
        s -> {
          var ctx = new BiStream(session, s);
          try {
            handler.onBiDirectionalStream(ctx);
          } catch (Exception e) {
            error(session, ctx, e);
          }
        });
    session.open();
  }

  private final void error(Session session, WtStream ctx, Exception e) {
    handler.onError(new WtError(session, e));
    // TODO check if it's right to close the entire session on error
    if (session.isOpen()) {
      ctx.closeSession(123, e.getMessage());
    }
  }
}
