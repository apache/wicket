package org.apache.wicket.protocol.ws.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.protocol.ws.WebSocketSettings;

public class WebSocketConnectionOriginFilter implements IWebSocketConnectionFilter {

    private final WebSocketSettings webSocketSettings;

    public WebSocketConnectionOriginFilter(WebSocketSettings webSocketSettings) {
        this.webSocketSettings = webSocketSettings;
    }

    @Override
    public void doFilter(HttpServletRequest servletRequest) {
        String oUrl = getOriginUrl(servletRequest);
        if (invalid(oUrl))
            // Send 403 Forbidden
            // Abort the WebSocket handshake
            throw new ConnectionRejectedException();
    }

    private boolean invalid(String oUrl) {
        if (originMismatch(oUrl))
            return true;
        if (oUrl == null || "".equals(oUrl))
            return true;
        return false;
    }

    private boolean originMismatch(String oUrl) {
        List<String> allowedDomains = webSocketSettings.getAllowedDomains();
        return !allowedDomains.contains(oUrl);
    }

    private String getOriginUrl(HttpServletRequest servletRequest) {
        ArrayList<String> origins = Collections.list(servletRequest.getHeaders("origin"));
        if (origins.size() != 1)
            return null;
        return origins.get(0);
    }

}
