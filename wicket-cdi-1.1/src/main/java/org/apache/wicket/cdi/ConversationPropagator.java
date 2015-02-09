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

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.BufferedResponseRequestHandler;
import org.apache.wicket.core.request.handler.IPageClassRequestHandler;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestHandlerDelegate;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A request cycle listener that takes care of propagating persistent
 * conversations.
 * 
 * @see ConversationScoped
 * 
 * @author igor
 */
public class ConversationPropagator extends AbstractRequestCycleListener
{
	private static final Logger logger = LoggerFactory.getLogger(ConversationPropagator.class);

	private static final MetaDataKey<Boolean> CONVERSATION_STARTED_KEY = new MetaDataKey<Boolean>()
	{
		private static final long serialVersionUID = 1L;
	};

	private static final MetaDataKey<String> CONVERSATION_ID_KEY = new MetaDataKey<String>()
	{
		private static final long serialVersionUID = 1L;
	};

	public static final String CID = "cid";

	/** propagation mode to use */
	private final IConversationPropagation propagation;

	private final Application application;

	@Inject
	private Conversation conversation;

	/**
	 * Constructor
	 * 
	 * @param container
	 * @param propagation
	 */
	public ConversationPropagator(Application application, IConversationPropagation propagation)
	{
		Args.notNull(application, "application");
		Args.notNull(propagation, "propagation");

		if (propagation == ConversationPropagation.NONE)
		{
			throw new IllegalArgumentException(
					"If propagation is NONE do not set up the propagator");
		}

		this.application = application;
		this.propagation = propagation;

		NonContextual.of(ConversationPropagator.class).postConstruct(this);
	}

	public IConversationPropagation getPropagation()
	{
		return propagation;
	}

	@Override
	public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler)
	{
		if (activateForHandler(handler))
		{
			logger.debug("Activating conversation {}", conversation.getId());
			fireOnAfterConversationStarted(cycle);
		}
	}

	private void fireOnAfterConversationStarted(RequestCycle cycle)
	{
		cycle.setMetaData(CONVERSATION_STARTED_KEY, true);
		for (IRequestCycleListener listener : application.getRequestCycleListeners())
		{
			if (listener instanceof ICdiAwareRequestCycleListener)
			{
				((ICdiAwareRequestCycleListener)listener).onAfterConversationActivated(cycle);
			}
		}
	}

	@Override
	public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler)
	{
		// propagate current non-transient conversation to the newly scheduled
		// page
		try
		{
			if (conversation.isTransient())
			{
				return;
			}
		} catch (ContextNotActiveException cnax)
		{
			logger.debug("There is no active context for the requested scope!", cnax);
			return;
		}

		if (propagation.propagatesVia(handler, getPage(handler)))
		{
			logger.debug(
					"Propagating non-transient conversation {} via page parameters of handler {}",
					conversation.getId(), handler);

			PageParameters parameters = getPageParameters(handler);
			if (parameters != null)
			{
				parameters.set(CID, conversation.getId());
				markPageWithConversationId(handler, conversation.getId());
			}
		}
	}


	@Override
	public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url)
	{
		// no need to propagate the conversation to packaged resources, they
		// should never change
		if (handler instanceof ResourceReferenceRequestHandler)
		{
			if (((ResourceReferenceRequestHandler)handler).getResourceReference() instanceof PackageResourceReference)
			{
				return;
			}
		}

		if (conversation.isTransient())
		{
			return;
		}

		if (propagation.propagatesVia(handler, getPage(handler)))
		{
			logger.debug("Propagating non-transient conversation {} via url", conversation.getId());
			url.setQueryParameter(CID, conversation.getId());
			markPageWithConversationId(handler, conversation.getId());
		}
	}

	@Override
	public void onDetach(RequestCycle cycle)
	{
		if (!Boolean.TRUE.equals(cycle.getMetaData(CONVERSATION_STARTED_KEY)))
		{
			return;
		}

		logger.debug("Deactivating conversation {}", conversation.getId());
		for (IRequestCycleListener listener : application.getRequestCycleListeners())
		{
			if (listener instanceof ICdiAwareRequestCycleListener)
			{
				((ICdiAwareRequestCycleListener)listener).onBeforeConversationDeactivated(cycle);
			}
		}
	}

	/**
	 * Determines whether or not a conversation should be activated fro the
	 * specified handler. This method is used to filter out conversation
	 * activation for utility handlers such as the
	 * {@link BufferedResponseRequestHandler}
	 * 
	 * @param handler
	 * @return {@code true} iff a conversation should be activated
	 */
	protected boolean activateForHandler(IRequestHandler handler)
	{
		if (handler != null)
		{
			if (handler instanceof BufferedResponseRequestHandler)
			{
				// we do not care about pages that are being rendered from a
				// buffer
				return false;
			}
		}
		return true;
	}

	public static void markPageWithConversationId(IRequestHandler handler, String cid)
	{
		Page page = getPage(handler);
		if (page != null)
		{
			page.setMetaData(CONVERSATION_ID_KEY, cid);
		}
	}

	public static String getConversationIdFromPage(Page page)
	{
		return page.getMetaData(CONVERSATION_ID_KEY);
	}

	public static void removeConversationIdFromPage(Page page)
	{
		page.setMetaData(CONVERSATION_ID_KEY, null);
	}

	/**
	 * Resolves a page instance from the request handler iff the page instance
	 * is already created
	 * 
	 * @param handler
	 * @return page or {@code null} if none
	 */
	public static Page getPage(IRequestHandler handler)
	{
		while (handler instanceof IRequestHandlerDelegate)
		{
			handler = ((IRequestHandlerDelegate)handler).getDelegateHandler();
		}

		if (handler instanceof IPageRequestHandler)
		{
			IPageRequestHandler pageHandler = (IPageRequestHandler)handler;
			if (pageHandler.isPageInstanceCreated())
			{
				return (Page)pageHandler.getPage();
			}
		}
		return null;
	}

	/**
	 * Resolves page parameters from a request handler
	 * 
	 * @param handler
	 * @return page parameters or {@code null} if none
	 */
	protected PageParameters getPageParameters(IRequestHandler handler)
	{
		if (handler instanceof IPageClassRequestHandler)
		{
			IPageClassRequestHandler pageHandler = (IPageClassRequestHandler)handler;
			return pageHandler.getPageParameters();
		}
		return null;
	}
}
