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

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
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
	private static MetaDataKey<AbstractCdiContainer> CONTEXT_KEY = new MetaDataKey<AbstractCdiContainer>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Constructor
	 */
	public AbstractCdiContainer()
	{
	}

	/**
	 * Deactivates conversational context
	 * 
	 * @param cycle
	 */
	public abstract void deactivateConversationalContext(RequestCycle cycle);

	/**
	 * Activates the conversational context and starts the conversation with the
	 * specified cid
	 * 
	 * @param cycle
	 * @param cid
	 */
	public abstract void activateConversationalContext(RequestCycle cycle, String cid);

	protected HttpServletRequest getRequest(RequestCycle cycle)
	{
		return (HttpServletRequest)cycle.getRequest().getContainerRequest();
	}

	/**
	 * Retrieves a conversation id, if any, that is associated with a
	 * {@link Page} instance
	 * 
	 * @param page
	 *            page instance
	 * @return conversation id, if any
	 */
	public String getConversationMarker(Page page)
	{
		return page.getMetaData(ConversationIdMetaKey.INSTANCE);
	}

	/**
	 * Removes conversation marker from the page instance which prevents the
	 * conversation from propagating to the page. This method should usually be
	 * called from page's {@code onDetach()} method.
	 * 
	 * @param page
	 */
	public void removeConversationMarker(Page page)
	{
		Args.notNull(page, "page");

		page.setMetaData(ConversationIdMetaKey.INSTANCE, null);
		page.getPageParameters().remove(ConversationPropagator.CID);
	}

	/**
	 * Binds this container instance to the {@link Application}, making it
	 * possible to retrieve it later
	 * 
	 * @param application
	 */
	protected void bind(Application application)
	{
		if (application.getMetaData(CONTEXT_KEY) != null)
		{
			throw new IllegalStateException("A CDI container is already bound to this "
					+ "application, which probably means you tried to configure the "
					+ "application twice");
		}
		application.setMetaData(CONTEXT_KEY, this);
	}

	/**
	 * Retrieves container instance stored in the application
	 * 
	 * @param application
	 * @return container instance or {@code null} if none
	 */
	public static AbstractCdiContainer get(Application application)
	{
		AbstractCdiContainer ctx = application.getMetaData(CONTEXT_KEY);
		if (ctx == null)
		{
			throw new IllegalStateException("No CDI Context bound to application");
		}
		return ctx;
	}

	/**
	 * Retrieves container instance stored in the current thread's application
	 * 
	 * @return container instance or {@code null} if none
	 */
	public static AbstractCdiContainer get()
	{
		return get(Application.get());
	}


}