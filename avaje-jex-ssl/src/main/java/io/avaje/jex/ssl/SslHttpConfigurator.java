package io.avaje.jex.ssl;

import javax.net.ssl.SSLContext;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;

/** HttpsConfigurator that allows you to force client authentication */
public class SslHttpConfigurator extends HttpsConfigurator {

  private final boolean clientAuth;

  public SslHttpConfigurator(SSLContext context, boolean clientAuth) {
    super(context);
    this.clientAuth = clientAuth;
  }

  @Override
  public void configure(HttpsParameters params) {
    var sslParams = getSSLContext().getDefaultSSLParameters();
    sslParams.setNeedClientAuth(clientAuth);
    params.setSSLParameters(sslParams);
  }
}
