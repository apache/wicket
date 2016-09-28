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

import javax.servlet.http.HttpServletRequest;

/**
 * Common interface for rejecting connections which are not meeting some of the security concerns.
 * One example might be when the connection 'Origin' header does not match the origin of the
 * application host
 *
 * @see WebSocketConnectionFilterCollection
 * @author Gergely Nagy
 */
public interface IWebSocketConnectionFilter {

    /**
     * Method for rejecting connections based on the current request
     *
     * @param servletRequest
     *            The servlet request holding the request headers
     */
    ConnectionRejected doFilter(HttpServletRequest servletRequest);
}
