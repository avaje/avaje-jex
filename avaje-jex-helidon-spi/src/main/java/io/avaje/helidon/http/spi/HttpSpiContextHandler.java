package io.avaje.helidon.http.spi;

import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.glassfish.grizzly.http.server.Response;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Authenticator.Result;
import com.sun.net.httpserver.Filter.Chain;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.org.slf4j.internal.LoggerFactory;

/**
 * Jetty handler that bridges requests to {@link HttpHandler}.
 */
public class HttpSpiContextHandler extends ContextHandler
{
    public static final Logger LOG = LoggerFactory.getLogger(HttpSpiContextHandler.class);

    private final HttpContext _httpContext;

    private HttpHandler _httpHandler;

    public HttpSpiContextHandler(HttpContext httpContext, HttpHandler httpHandler)
    {
        this._httpContext = httpContext;
        this._httpHandler = httpHandler;
        // The default jax-ws web server allows posting to URLs that do not end
        // with a trailing '/'; allow it too to be a drop-in replacement.
        setAllowNullPathInContext(true);
        super.setHandler(new Handler.Abstract()
        {
            @Override
            public boolean handle(Request request, Response response, Callback callback)
            {
                try (HttpExchange jettyHttpExchange = request.isSecure()
                    ? new JettyHttpsExchange(_httpContext, request, response)
                    : new JettyHttpExchange(_httpContext, request, response))
                {
                    Authenticator auth = _httpContext.getAuthenticator();
                    if (auth != null && handleAuthentication(request, response, callback, jettyHttpExchange, auth))
                        return true;

                    new Chain(_httpContext.getFilters(), _httpHandler).doFilter(jettyHttpExchange);
                    callback.succeeded();
                }
                catch (Exception ex)
                {
                    LOG.debug("Failed to handle", ex);
                    Response.writeError(request, response, callback, 500, null, ex);
                }
                return true;
            }
        });
    }

    @Override
    public void setHandler(Handler handler)
    {
        throw new UnsupportedOperationException();
    }

    private boolean handleAuthentication(
        Request request,
        Response response,
        Callback callback,
        HttpExchange httpExchange,
        Authenticator auth)
    {
        Result result = auth.authenticate(httpExchange);
        if (result instanceof Authenticator.Failure)
        {
            int rc = ((Authenticator.Failure)result).getResponseCode();
            for (Map.Entry<String, List<String>> header : httpExchange.getResponseHeaders().entrySet())
            {
                for (String value : header.getValue())
                    response.getHeaders().add(header.getKey(), value);
            }
            Response.writeError(request, response, callback, rc);
            return true;
        }

        if (result instanceof Authenticator.Retry)
        {
            int rc = ((Authenticator.Retry)result).getResponseCode();
            for (Map.Entry<String, List<String>> header : httpExchange.getResponseHeaders().entrySet())
            {
                for (String value : header.getValue())
                {
                    response.getHeaders().add(header.getKey(), value);
                }
            }
            Response.writeError(request, response, callback, rc);
            return true;
        }

        if (result instanceof Authenticator.Success)
        {
            HttpPrincipal principal = ((Authenticator.Success)result).getPrincipal();
            ((JettyExchange)httpExchange).setPrincipal(principal);
            return false;
        }

        Response.writeError(request, response, callback, 500);
        return true;
    }

    public HttpHandler getHttpHandler()
    {
        return _httpHandler;
    }

    public void setHttpHandler(HttpHandler handler)
    {
        this._httpHandler = handler;
    }
}