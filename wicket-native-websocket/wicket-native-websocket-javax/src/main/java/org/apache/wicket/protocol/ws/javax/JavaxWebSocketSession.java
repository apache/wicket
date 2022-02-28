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
package org.apache.wicket.protocol.ws.javax;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.websocket.Session;

import org.apache.wicket.protocol.ws.api.IWebSocketSession;

/**
 * An implementation of IWebSocketSession that is just a delegating  wrapper around {@link javax.websocket.Session}.
 */
class JavaxWebSocketSession implements IWebSocketSession
{
    private final Session session;

    JavaxWebSocketSession(Session session)
    {
        this.session = session;
    }

    @Override
    public String getProtocolVersion()
    {
        return session.getProtocolVersion();
    }

    @Override
    public String getNegotiatedSubprotocol()
    {
        return session.getNegotiatedSubprotocol();
    }

    @Override
    public boolean isSecure()
    {
        return session.isSecure();
    }

    @Override
    public boolean isOpen()
    {
        return session.isOpen();
    }

    @Override
    public long getMaxIdleTimeout()
    {
        return session.getMaxIdleTimeout();
    }

    @Override
    public void setMaxIdleTimeout(long milliseconds)
    {
        session.setMaxIdleTimeout(milliseconds);
    }

    @Override
    public void setMaxBinaryMessageBufferSize(int length)
    {
        session.setMaxBinaryMessageBufferSize(length);
    }

    @Override
    public int getMaxBinaryMessageBufferSize()
    {
        return session.getMaxBinaryMessageBufferSize();
    }

    @Override
    public void setMaxTextMessageBufferSize(int length)
    {
        session.setMaxTextMessageBufferSize(length);
    }

    @Override
    public int getMaxTextMessageBufferSize()
    {
        return session.getMaxTextMessageBufferSize();
    }

    @Override
    public String getId()
    {
        return session.getId();
    }

    @Override
    public URI getRequestURI()
    {
        return session.getRequestURI();
    }

    @Override
    public Map<String, List<String>> getRequestParameterMap()
    {
        return session.getRequestParameterMap();
    }

    @Override
    public String getQueryString()
    {
        return session.getQueryString();
    }

    @Override
    public Map<String, String> getPathParameters()
    {
        return session.getPathParameters();
    }

    @Override
    public Map<String, Object> getUserProperties()
    {
        return session.getUserProperties();
    }

    @Override
    public Principal getUserPrincipal()
    {
        return session.getUserPrincipal();
    }
}
