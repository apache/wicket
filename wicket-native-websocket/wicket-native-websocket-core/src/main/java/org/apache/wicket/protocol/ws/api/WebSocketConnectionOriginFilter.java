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

public class WebSocketConnectionOriginFilter implements IWebSocketConnectionFilter {

    private final WebSocketSettings webSocketSettings;

    public WebSocketConnectionOriginFilter(WebSocketSettings webSocketSettings) {
        this.webSocketSettings = webSocketSettings;
    }

    @Override
    public void doFilter(HttpServletRequest servletRequest) {
        if (webSocketSettings.isHijackingProtectionEnabled()) {
            String oUrl = getOriginUrl(servletRequest);
            if (invalid(oUrl))
                throw new ConnectionRejectedException();
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
