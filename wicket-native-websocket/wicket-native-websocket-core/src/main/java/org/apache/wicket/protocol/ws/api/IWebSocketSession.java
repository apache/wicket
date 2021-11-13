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

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Wicket proxy interface to javax.websocket.Session.
 */
public interface IWebSocketSession
{

    /**
     * See javax.websocket.Session#getProtocolVersion()
     */
    String getProtocolVersion();


    /**
     * See javax.websocket.Session#getNegotiatedSubprotocol()
     */
    String getNegotiatedSubprotocol();


    /**
     * See javax.websocket.Session#isSecure()
     */
    boolean isSecure();

    /**
     * See javax.websocket.Session#isOpen()
     */
    boolean isOpen();

    /**
     * See javax.websocket.Session#getMaxIdleTimeout()
     */
    long getMaxIdleTimeout();

    /**
     * See javax.websocket.Session#setMaxIdleTimeout()
     */
    void setMaxIdleTimeout(long milliseconds);

    /**
     * See javax.websocket.Session#setMaxBinaryMessageBufferSize()
     */
    void setMaxBinaryMessageBufferSize(int length);

    /**
     * See javax.websocket.Session#getMaxBinaryMessageBufferSize()
     */
    int getMaxBinaryMessageBufferSize();

    /**
     * See javax.websocket.Session#setMaxTextMessageBufferSize()
     */
    void setMaxTextMessageBufferSize(int length);

    /**
     * See javax.websocket.Session#getMaxTextMessageBufferSize()
     */
    int getMaxTextMessageBufferSize();


    /**
     * See javax.websocket.Session#getId()
     */
    String getId();


    /**
     * See javax.websocket.Session#getRequestURI()
     */
    URI getRequestURI();

    /**
     * See javax.websocket.Session#getRequestParameterMap()
     */
    Map<String, List<String>> getRequestParameterMap();

    /**
     * See javax.websocket.Session#getQueryString()
     */
    String getQueryString();

    /**
     * See javax.websocket.Session#getPathParameters()
     */
    Map<String, String> getPathParameters();

    /**
     * See javax.websocket.Session#getUserProperties()
     */
    Map<String, Object> getUserProperties();

    /**
     * See javax.websocket.Session#getUserPrincipal()
     */
    Principal getUserPrincipal();
}
