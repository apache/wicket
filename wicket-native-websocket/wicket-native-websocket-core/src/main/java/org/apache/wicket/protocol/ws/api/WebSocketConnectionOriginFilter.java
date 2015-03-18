package org.apache.wicket.protocol.ws.api;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.request.Url;

public class WebSocketConnectionOriginFilter implements IWebSocketConnectionFilter {

    @Override
    public void doFilter(HttpServletRequest servletRequest) {
        Url oUrl = getOriginUrl(servletRequest);
        Url rUrl = getRequestUrl(servletRequest);
        if (invalid(oUrl) || invalid(rUrl) || originMismatch(oUrl, rUrl))
            // Send 403 Forbidden
            // Abort the WebSocket handshake
            throw new ConnectionRejectedException();
    }

    private boolean invalid(Url url) {
        if (url == null || url.getProtocol() == null || "".equals(url.getProtocol()) || url.getHost() == null || "".equals(url.getHost())
                || url.getPort() == null)
            return true;
        return false;
    }

    private boolean originMismatch(Url oUrl, Url rUrl) {
        return !oUrl.getPort().equals(rUrl.getPort()) || !oUrl.getHost().equals(rUrl.getHost()) || !oUrl.getPort().equals(rUrl.getPort());
    }

    private Url getRequestUrl(HttpServletRequest servletRequest) {
        Url url = new Url();
        url.setProtocol("http");
        url.setHost(servletRequest.getServerName());
        url.setPort(servletRequest.getServerPort());
        return url;
    }

    private Url getOriginUrl(HttpServletRequest servletRequest) {
        String rOrigin = servletRequest.getHeader("origin");
        Url oUrl = Url.parse(rOrigin);
        return oUrl;
    }

}
