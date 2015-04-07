/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.protocol.ws.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.protocol.ws.WebSocketSettings;

/**
 * This filter will reject those requests which contain 'Origin' header that does not match the origin of the
 * application host. This kind of extended security might be necessary if the application needs to enforce the
 * Same Origin Policy which is not provided by the HTML5 WebSocket protocol.
 *
 * @see <a href="http://www.christian-schneider.net/CrossSiteWebSocketHijacking.html">http://www.christian-schneider.net/CrossSiteWebSocketHijacking.html</a>
 *
 * @author Gergely Nagy
 *
 */
public class WebSocketConnectionOriginFilter implements IWebSocketConnectionFilter {

    /**
     * 1008 indicates that an endpoint is terminating the connection because it has received a message that violates its policy. This is a generic status code
     * that can be returned when there is no other more suitable status code (e.g., 1003 or 1009) or if there is a need to hide specific details about the
     * policy.
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455, Section 7.4.1 Defined Status Codes</a>.
     */
    public static final int POLICY_VIOLATION = 1008;

    /**
     * Explanatory text for the client to explain why the connection is getting aborted
     */
    public static final String ORIGIN_MISMATCH = "Origin mismatch";

    private final WebSocketSettings webSocketSettings;

    public WebSocketConnectionOriginFilter(WebSocketSettings webSocketSettings) {
        this.webSocketSettings = webSocketSettings;
    }

    @Override
    public void doFilter(HttpServletRequest servletRequest) {
        if (webSocketSettings.isHijackingProtectionEnabled()) {
            String oUrl = getOriginUrl(servletRequest);
            if (invalid(oUrl))
                throw new ConnectionRejectedException(POLICY_VIOLATION, ORIGIN_MISMATCH);
        }
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
        ArrayList<String> origins = Collections.list(servletRequest.getHeaders("Origin"));
        if (origins.size() != 1)
            return null;
        return origins.get(0);
    }

}
