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

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.event.WebSocketPayload;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Classes;

/**
 * An {@link org.apache.wicket.request.IRequestHandler} that broadcasts the payload to the
 * page/resource
 */
public class WebSocketMessageBroadcastHandler implements IRequestHandler
{
	private final int pageId;
	private final String resourceName;
	private final WebSocketPayload<?> payload;

	/**
	 * Constructor.
	 *
	 * @param pageId
	 *          The id of the page if {@link org.apache.wicket.protocol.ws.api.WebSocketBehavior}
	 *          or {@value org.apache.wicket.protocol.ws.api.AbstractWebSocketProcessor#NO_PAGE_ID} if using a resource
	 * @param resourceName
	 *          The name of the shared {@link org.apache.wicket.protocol.ws.api.WebSocketResource}
	 * @param payload
	 *          The payload to broadcast
	 */
	WebSocketMessageBroadcastHandler(int pageId, String resourceName, WebSocketPayload<?> payload)
	{
		this.pageId = pageId;
		this.resourceName = resourceName;
		this.payload = Args.notNull(payload, "payload");
	}

	@Override
	public void respond(IRequestCycle requestCycle)
	{
		final Application application = Application.get();

		final Runnable action = new Runnable()
		{
			@Override
			public void run()
			{
				if (pageId != AbstractWebSocketProcessor.NO_PAGE_ID)
				{
					Page page = (Page) Session.get().getPageManager().getPage(pageId);
					page.send(application, Broadcast.BREADTH, payload);
				}
				else
				{
					ResourceReference reference = new SharedResourceReference(resourceName);
					IResource resource = reference.getResource();
					if (resource instanceof WebSocketResource)
					{
						WebSocketResource wsResource = (WebSocketResource) resource;
						wsResource.onPayload(payload);
					}
					else
					{
						throw new IllegalStateException(
								String.format("Shared resource with name '%s' is not a %s but %s",
										resourceName, WebSocketResource.class.getSimpleName(),
										Classes.name(resource.getClass())));
					}
				}
			}
		};

		WebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);
		webSocketSettings.getSendPayloadExecutor().run(action);
	}

	@Override
	public void detach(IRequestCycle requestCycle)
	{
	}
}
