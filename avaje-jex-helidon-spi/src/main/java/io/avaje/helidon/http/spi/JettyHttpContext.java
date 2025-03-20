package io.avaje.helidon.http.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Jetty implementation of {@link com.sun.net.httpserver.HttpContext}
 */
public class JettyHttpContext extends com.sun.net.httpserver.HttpContext
{

    private final HttpSpiContextHandler _jettyContextHandler;

    private final HttpServer _server;

    private final Map<String, Object> _attributes = new HashMap<String, Object>();

    private final List<Filter> _filters = new ArrayList<Filter>();

    private Authenticator _authenticator;

    protected JettyHttpContext(HttpServer server, String contextPath, HttpHandler handler)
    {
        this._server = server;
        _jettyContextHandler = new HttpSpiContextHandler(this, handler);
        _jettyContextHandler.setContextPath(contextPath);
    }

    protected HttpSpiContextHandler getJettyContextHandler()
    {
        return _jettyContextHandler;
    }

    @Override
    public HttpHandler getHandler()
    {
        return _jettyContextHandler.getHttpHandler();
    }

    @Override
    public void setHandler(HttpHandler h)
    {
        _jettyContextHandler.setHttpHandler(h);
    }

    @Override
    public String getPath()
    {
        return _jettyContextHandler.getContextPath();
    }

    @Override
    public HttpServer getServer()
    {
        return _server;
    }

    @Override
    public Map<String, Object> getAttributes()
    {
        return _attributes;
    }

    @Override
    public List<Filter> getFilters()
    {
        return _filters;
    }

    @Override
    public Authenticator setAuthenticator(Authenticator auth)
    {
        Authenticator previous = _authenticator;
        _authenticator = auth;
        return previous;
    }

    @Override
    public Authenticator getAuthenticator()
    {
        return _authenticator;
    }
}
