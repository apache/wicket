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

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Args;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.http.Http;

/**
 * Provides access to CDI features from inside a Wicket request
 * 
 * @author igor
 * 
 */
public class CdiContainer
{
	private static final MetaDataKey<CdiContainer> CONTEXT_KEY = new MetaDataKey<CdiContainer>()
	{
		private static final long serialVersionUID = 1L;
	};

	protected final BeanManager beanManager;
//	private final BoundContext<HttpServletRequest> conversationContext;
	private final INonContextualManager nonContextualManager;

	@Inject 
	@Http 
	ConversationContext conversationContext;
	/**
	 * Constructor
	 * 
	 * @param beanManager
	 *            bean manager
	 */
	public CdiContainer(BeanManager beanManager, INonContextualManager nonContextualManager)
	{
		Args.notNull(beanManager, "beanManager");
		Args.notNull(nonContextualManager, "nonContextualManager");

		this.beanManager = beanManager;
		this.nonContextualManager = nonContextualManager;
		
		try 
		{
			Class.forName("org.jboss.weld.context.ConversationContext");
		} 
		catch (ClassNotFoundException cnfe) 
		{
			throw new IllegalStateException("Could not find Weld 2.0 Context. Make sure a Weld 2.0 module for your CDI container implementation is included in your dependencies.");
		}
        }

	public INonContextualManager getNonContextualManager()
	{
		return nonContextualManager;
	}

	/**
	 * Deactivates conversational context
	 * 
	 * @param cycle
	 */
	public void deactivateConversationalContext(RequestCycle cycle)
	{
		conversationContext.deactivate();		
	}

	/**
	 * Activates the conversational context and starts the conversation with the specified cid
	 * 
	 * @param cycle
	 * @param cid
	 */
	public void activateConversationalContext(RequestCycle cycle, String cid)
	{
		if(conversationContext.isActive())
		{
			deactivateConversationalContext(cycle);
		}
		conversationContext.activate(cid);
	}

	/**
	 * Retrieves a conversation id, if any, that is associated with a {@link Page} instance
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
		page.getPageParameters().remove(ConversationPropagator.CID);
	}

	/**
	 * Binds this container instance to the {@link Application}, making it possible to retrieve it
	 * later
	 * 
	 * @param application
	 */
	protected void bind(Application application)
	{
		application.setMetaData(CONTEXT_KEY, this);
	}

	/**
	 * Retrieves container instance stored in the application
	 * 
	 * @param application
	 * @return container instance or {@code null} if none
	 */
	public static final CdiContainer get(Application application)
	{
		CdiContainer ctx = application.getMetaData(CONTEXT_KEY);
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
	public static final CdiContainer get()
	{
		return get(Application.get());
	}


}