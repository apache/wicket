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
package org.apache.wicket.protocol.ws;

import org.apache.wicket.protocol.http.ResourceIsolationRequestCycleListener;
import org.apache.wicket.protocol.ws.api.WebSocketMessageBroadcastHandler;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.request.IRequestHandler;

/**
 * A specialization of {@link ResourceIsolationRequestCycleListener} that should be
 * used when the application uses Web Sockets.
 *
 * <p>The HTTP upgrade request brings <em>Origin</em> and/or <em>Fetch Metadata</em>
 * in its headers, but any Web socket frame doesn't bring it so
 * {@link WebSocketRequestHandler} and {@link WebSocketMessageBroadcastHandler}
 * should be ignored.</p>
 */
public class WebSocketAwareResourceIsolationRequestCycleListener extends ResourceIsolationRequestCycleListener
{
	@Override
	protected boolean isChecked(IRequestHandler handler)
	{
		if (handler instanceof WebSocketRequestHandler || handler instanceof WebSocketMessageBroadcastHandler) {
			return false;
		}
		return super.isChecked(handler);
	}
}
