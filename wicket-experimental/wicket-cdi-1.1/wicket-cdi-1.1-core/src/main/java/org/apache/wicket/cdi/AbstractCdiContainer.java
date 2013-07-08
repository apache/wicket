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
package org.apache.wicket.cdi;

import javax.enterprise.context.Conversation;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Page;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Args;

/**
 * Provides access to CDI features from inside a Wicket request
 *
 * @author igor
 */
public abstract class AbstractCdiContainer
{

	/**
	 * Activates the conversational context and starts the conversation with the specified cid
	 *
	 * @param cycle
	 * @param cid
	 */
	public abstract void activateConversationalContext(RequestCycle cycle, String cid);

	/**
	 * Retrieve the current conversation associated with the ConversationContext
	 *
	 * @return The current Conversation attached to the current Conversation Context
	 */
	public abstract Conversation getCurrentConversation();

	protected HttpServletRequest getRequest(RequestCycle cycle)
	{
		return (HttpServletRequest) cycle.getRequest().getContainerRequest();
	}

	/**
	 * Retrieves a conversation id, if any, that is associated with a {@link Page} instance
	 *
	 * @param page page instance
	 * @return conversation id, if any
	 */
	public String getConversationMarker(Page page)
	{
		return page.getMetaData(ConversationIdMetaKey.INSTANCE);
	}

	/**
	 * Removes conversation marker from the page instance which prevents the conversation from
	 * propagating to the page. This method should usually be called from page's {@code onDetach()}
	 * method.
	 *
	 * @param page
	 */
	public void removeConversationMarker(Page page)
	{
		Args.notNull(page, "page");

		page.setMetaData(ConversationIdMetaKey.INSTANCE, null);
		page.getPageParameters().remove(ConversationPropagator.CID_ATTR);
	}

}
