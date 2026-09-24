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

import org.apache.wicket.protocol.ws.api.event.WebSocketAbortedPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketBinaryPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketClosedPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketConnectedPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketErrorPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketPushPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketTextPayload;
import org.apache.wicket.protocol.ws.api.message.AbortedMessage;
import org.apache.wicket.protocol.ws.api.message.BinaryMessage;
import org.apache.wicket.protocol.ws.api.message.ClosedMessage;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.ErrorMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.request.resource.IResource;

/**
 * An IResource that can be used as WebSocket endpoint
 * <p>
 * Unlike other resources this one is never served over HTTP - {@link #respond(Attributes)} is final
 * and empty - so a WebSocket connection is the only way to reach it. Register it as a shared
 * resource and let a {@link BaseWebSocketBehavior} name it, and every callback below is driven by
 * the client that opened the connection.
 * <p>
 * The {@link org.apache.wicket.authorization.IAuthorizationStrategy} of the application is asked
 * whether that client may use this resource, through
 * {@link org.apache.wicket.authorization.IAuthorizationStrategy#isResourceAuthorized(IResource, org.apache.wicket.request.mapper.parameter.PageParameters)},
 * so a restriction on it - for example the {@code AuthorizeResource} annotation of wicket-auth-roles
 * - is enforced. A refused client is disconnected and reaches none of the callbacks, not even
 * {@link #onClose(org.apache.wicket.protocol.ws.api.message.ClosedMessage)} or
 * {@link #onAbort(org.apache.wicket.protocol.ws.api.message.AbortedMessage)}.
 * <p>
 * The question is put once, when the connection is opened, and is not repeated for the messages
 * that follow: a connection that was allowed stays allowed for as long as it is open, even if the
 * privileges of the session change meanwhile. That is deliberate - this resource exists to carry
 * many messages cheaply, without the page lock a {@link WebSocketBehavior} takes, and a registry
 * lookup and a strategy call in front of every message would tax exactly that. The connection
 * does end with the HTTP session it was opened on, but privileges withdrawn from a session that
 * lives on are not noticed: close the connection from the application when that happens.
 */
public abstract class WebSocketResource implements IResource
{
	void onPayload(WebSocketPayload<?> payload)
	{
		WebSocketRequestHandler webSocketHandler = payload.getHandler();

		if (payload instanceof WebSocketTextPayload)
		{
			WebSocketTextPayload textPayload = (WebSocketTextPayload) payload;
			TextMessage data = textPayload.getMessage();
			onMessage(webSocketHandler, data);
		}
		else if (payload instanceof WebSocketBinaryPayload)
		{
			WebSocketBinaryPayload binaryPayload = (WebSocketBinaryPayload) payload;
			BinaryMessage binaryData = binaryPayload.getMessage();
			onMessage(webSocketHandler, binaryData);
		}
		else if (payload instanceof WebSocketConnectedPayload)
		{
			WebSocketConnectedPayload connectedPayload = (WebSocketConnectedPayload) payload;
			ConnectedMessage message = connectedPayload.getMessage();
			onConnect(message);
		}
		else if (payload instanceof WebSocketClosedPayload)
		{
			WebSocketClosedPayload connectedPayload = (WebSocketClosedPayload) payload;
			ClosedMessage message = connectedPayload.getMessage();
			onClose(message);
		}
		else if (payload instanceof WebSocketErrorPayload)
		{
			WebSocketErrorPayload errorPayload = (WebSocketErrorPayload) payload;
			ErrorMessage message = errorPayload.getMessage();
			onError(webSocketHandler, message);
		}
		else if (payload instanceof WebSocketAbortedPayload)
		{
			WebSocketAbortedPayload abortedPayload = (WebSocketAbortedPayload) payload;
			AbortedMessage message = abortedPayload.getMessage();
			onAbort(message);
		}
		else if (payload instanceof WebSocketPushPayload)
		{
			WebSocketPushPayload pushPayload = (WebSocketPushPayload) payload;
			IWebSocketPushMessage message = pushPayload.getMessage();
			onPush(webSocketHandler, message);
		}
	}

	/**
	 * A callback method called when there is a message pushed/broadcasted by the
	 * server, e.g. pushed by a backend service
	 *
	 * @param handler
	 *          The request handler that can be used to send messages to the client
	 * @param message
	 *          The message pushed/broadcasted by the server
	 */
	protected void onPush(WebSocketRequestHandler handler, IWebSocketPushMessage message)
	{
	}

	/**
	 * A callback method called when a WebSocket client has connected to the endpoint
	 * handled by this WebSocketBehavior
	 *
	 * @param message
	 *          the connect message with the info about the client
	 */
	protected void onConnect(ConnectedMessage message)
	{
	}

    /**
     * A callback method called when the server has aborted the connection
     *
     * @param message
     *          the aborted message with the info about the server
     */
    protected void onAbort(AbortedMessage message)
    {
    }

	/**
	 * A callback method called when a WebSocket client has closed the connection
	 * to the endpoint handled by this WebSocketBehavior
	 *
	 * @param message
	 *          the close message with the info about the client
	 */
	protected void onClose(ClosedMessage message)
	{
	}

	/**
	 * A callback method called when there is a communication error
	 *
	 * @param handler
	 *          The request handler that can be used to send messages to the client
	 * @param message
	 *          The error message that that brings information about the communication error
	 */
	protected void onError(WebSocketRequestHandler handler, ErrorMessage message)
	{
	}

	/**
	 * A callback method called when there is a text message sent by the client
	 *
	 * @param handler
	 *          The request handler that can be used to send messages back to the client
	 * @param message
	 *          The text message sent by the client
	 */
	protected void onMessage(WebSocketRequestHandler handler, TextMessage message)
	{
	}

	/**
	 * A callback method called when there is a binary message sent by the client
	 *
	 * @param handler
	 *          The request handler that can be used to send messages back to the client
	 * @param binaryMessage
	 *          The binary message sent by the client
	 */
	protected void onMessage(WebSocketRequestHandler handler, BinaryMessage binaryMessage)
	{
	}

	/**
	 * Does nothing, deliberately: a WebSocketResource has no representation to serve over HTTP, and
	 * a request for one writes an empty response. Its callbacks are reached over a WebSocket
	 * connection instead, which is where the
	 * {@link org.apache.wicket.authorization.IAuthorizationStrategy} is asked about it - so unlike
	 * the note on {@link IResource#respond(Attributes)}, nothing is served here without being
	 * authorized, because nothing is served here at all.
	 */
	@Override
	public final void respond(Attributes attributes)
	{
	}
}
