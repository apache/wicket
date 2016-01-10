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

import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.http.WebRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * A {@link WebRequest} implementation used for the lifecycle of a web socket
 * connection. It keeps a copy of the HttpServletRequest provided by the web container
 * during the creation of the web socket connection (the http upgrade).
 *
 * @since 6.0
 */
public class WebSocketRequest extends ServletWebRequest
{
	/**
	 * Constructor.
	 *
	 * @param req
	 *      the copy of the HttpServletRequest used for the upgrade of the HTTP protocol
	 */
	public WebSocketRequest(HttpServletRequest req, String filterPrefix)
	{
		super(req, filterPrefix);
	}

	@Override
	public boolean isAjax()
	{
		return true;
	}
}
