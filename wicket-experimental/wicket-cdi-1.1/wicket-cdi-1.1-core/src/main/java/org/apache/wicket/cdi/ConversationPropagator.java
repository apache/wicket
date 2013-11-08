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

import java.io.Serializable;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.NonexistentConversationException;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.BufferedResponseRequestHandler;
import org.apache.wicket.core.request.handler.IPageClassRequestHandler;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.core.request.mapper.StalePageException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestHandlerDelegate;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.util.visit.Visits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A request cycle listener that takes care of propagating persistent conversations.
 * 
 * @see ConversationScoped
 * 
 * @author igor
 */
@ConversationScoped
public class ConversationPropagator extends AbstractRequestCycleListener implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(ConversationPropagator.class);

	private static final MetaDataKey<String> CID_KEY = ConversationIdMetaKey.INSTANCE;

	private static final MetaDataKey<Boolean> CONVERSATION_STARTED_KEY = new MetaDataKey<Boolean>()
	{
	};

	static final String CID_ATTR = "cid";
       
	@Inject
	Instance<AbstractCdiContainer> containerSource;

	/** propagation mode to use */
	@Propagation
	@Inject
	Instance<IConversationPropagation> propagationSource;
	
	@Auto
	@Inject
	Instance<Boolean> auto;

	@Inject
	Conversation conversation_;

	/**
	 * Constructor
	 * 
	 * @param container
	 * @param propagation
	 */
	public ConversationPropagator()
	{
	}

        
	private Conversation getConversation(RequestCycle cycle)
	{
		return Boolean.TRUE.equals(cycle.getMetaData(CONVERSATION_STARTED_KEY)) ? conversation_
			: null;
	}

	@Override
	public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler)
	{
		String cid = cycle.getRequest().getRequestParameters().getParameterValue(CID_ATTR).toString();
		Page page = getPage(handler);

		if (cid == null && page != null)
		{
			cid = page.getMetaData(CID_KEY);
		}

		Conversation current = getConversation(cycle);
		if (current != null && !Objects.isEqual(current.getId(), cid))
		{
			logger.info("Conversation {} has expired for {}", cid, page);
			throw new ConversationExpiredException(null, cid, page, handler);
		}

		activateConversationIfNeeded(cycle, handler, cid);
	}

	@Override
	public IRequestHandler onException(RequestCycle cycle, Exception ex)
	{
		// if we are handling a stale page exception then use its conversation since we are most
		// likely about to rerender it.

		if (ex instanceof StalePageException)
		{
			IRequestablePage requestable = ((StalePageException)ex).getPage();
			if (requestable instanceof Page)
			{
				String cid = containerSource.get().getConversationMarker((Page)requestable);
				if (cid != null)
				{
					try
					{
						activateConversationIfNeeded(cycle, null, cid);
						return null;
					}
					catch (ConversationExpiredException e)
					{
						// ignore, we will start a new one below
					}
				}
			}
		}

		activateConversationIfNeeded(cycle, null, null);
		return null;
	}

	private void activateConversationIfNeeded(RequestCycle cycle, IRequestHandler handler,
		String cid)
	{
		Conversation current = getConversation(cycle);

		if (current != null || !activateForHandler(handler))
		{
			return;
		}

		logger.debug("Activating conversation {}", cid);

		try
		{
			containerSource.get().activateConversationalContext(cycle, cid);
			fireOnAfterConversationStarted(cycle);
		}
		catch (NonexistentConversationException e)
		{
			logger.info("Unable to restore conversation with id {}", cid, e.getMessage());
			logger.debug("Unable to restore conversation", e);
			fireOnAfterConversationStarted(cycle);
			throw new ConversationExpiredException(e, cid, getPage(handler), handler);
		}

		cycle.setMetaData(CONVERSATION_STARTED_KEY, true);
	}

	private void fireOnAfterConversationStarted(RequestCycle cycle)
	{
		for (IRequestCycleListener listener : Application.get().getRequestCycleListeners())
		{
			if (listener instanceof ICdiAwareRequestCycleListener)
			{
				((ICdiAwareRequestCycleListener)listener).onAfterConversationActivated(cycle);
			}
		}
	}

	@Override
	public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler)
	{
		Conversation conversation = getConversation(cycle);

		if (conversation == null)
		{
			return;
		}

		Page page = getPage(handler);

		if (page == null)
		{
			return;
		}

		// apply auto semantics

		autoEndIfNecessary(page, handler, conversation);
		autoBeginIfNecessary(page, handler, conversation);

		if (propagationSource.get().propagatesViaPage(page, handler))
		{
			// propagate a conversation across non-bookmarkable page instances
			setConversationOnPage(conversation, page);
		}
	}

	@Override
	public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler)
	{
		// propagate current non-transient conversation to the newly scheduled page

		Conversation conversation = getConversation(cycle);

		if (conversation == null || conversation.isTransient())
		{
			return;
		}

		Page page = getPage(handler);
		if (page != null)
		{
			if (propagationSource.get().propagatesViaPage(page, handler))
			{
				// propagate a conversation across non-bookmarkable page instances
				setConversationOnPage(conversation, page);
			}
		}

		if (propagationSource.get().propagatesViaParameters(handler))
		{
			// propagate cid to a scheduled bookmarkable page

			logger.debug(
				"Propagating non-transient conversation {} via page parameters of handler {}",
				conversation.getId(), handler);

			PageParameters parameters = getPageParameters(handler);
			if (parameters != null)
			{
				parameters.set(CID_ATTR, conversation.getId());
			}
		}
	}

	protected void setConversationOnPage(Conversation conversation, Page page)
	{
		if (conversation == null || conversation.isTransient())
		{
			logger.debug("Detaching transient conversation {} via meta of page instance {}",
				(conversation == null ? "null" : conversation.getId()), page);

			page.setMetaData(CID_KEY, null);
		}
		else
		{

			logger.debug("Propagating non-transient conversation {} via meta of page instance {}",
				conversation.getId(), page);

			page.setMetaData(CID_KEY, conversation.getId());
		}
	}

	@Override
	public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url)
	{
		// no need to propagate the conversation to packaged resources, they should never change
		if (handler instanceof ResourceReferenceRequestHandler)
		{
			if (((ResourceReferenceRequestHandler)handler).getResourceReference() instanceof PackageResourceReference)
			{
				return;
			}
		}

		Conversation conversation = getConversation(cycle);

		if (conversation == null || conversation.isTransient())
		{
			return;
		}

		if (propagationSource.get().propagatesViaParameters(handler))
		{
			// propagate cid to bookmarkable pages via urls

			logger.debug("Propagating non-transient conversation {} via url", conversation.getId());

			url.setQueryParameter(CID_ATTR, conversation.getId());
		}
	}

	@Override
	public void onDetach(RequestCycle cycle)
	{
		Conversation conversation = getConversation(cycle);
		if (conversation != null)
		{
			logger.debug("Deactivating conversation {}", conversation.getId());

			for (IRequestCycleListener listener : Application.get().getRequestCycleListeners())
			{
				if (listener instanceof ICdiAwareRequestCycleListener)
				{
					((ICdiAwareRequestCycleListener)listener).onBeforeConversationDeactivated(cycle);
				}
			}
			containerSource.get().deactivateConversationalContext(cycle);

			cycle.setMetaData(CONVERSATION_STARTED_KEY, null);
		}
	}

	/**
	 * Determines whether or not a conversation should be activated fro the specified handler. This
	 * method is used to filter out conversation activation for utility handlers such as the
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
				// we do not care about pages that are being rendered from a buffer
				return false;
			}
		}
		return true;
	}

	protected void autoBeginIfNecessary(Page page, IRequestHandler handler,
		Conversation conversation)
	{
		if (!auto.get() || conversation == null || !conversation.isTransient() || page == null ||
			!propagationSource.get().propagatesViaPage(page, handler) || !hasConversationalComponent(page))
		{
			return;
		}

		// auto activate conversation

		conversation.begin();

		logger.debug("Auto-began conversation {} for page {}", conversation.getId(), page);
	}

	protected void autoEndIfNecessary(Page page, IRequestHandler handler, Conversation conversation)
	{
		if (!auto.get() || conversation == null || conversation.isTransient() || page == null ||
			!propagationSource.get().propagatesViaPage(page, handler) || hasConversationalComponent(page))
		{
			return;
		}

		// auto de-activate conversation

		String cid = conversation.getId();

		conversation.end();

		logger.debug("Auto-ended conversation {} for page {}", cid, page);
	}


	protected boolean hasConversationalComponent(Page page)
	{
		Boolean hasConversational = Visits.visit(page, new IVisitor<Component, Boolean>()
		{
			@Override
			public void component(Component object, IVisit<Boolean> visit)
			{
				if (object instanceof ConversationalComponent)
				{
					visit.stop(true);
				}
			}
		});

		return hasConversational == null ? false : hasConversational;
	}

	/**
	 * Resolves a page instance from the request handler iff the page instance is already created
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

    Boolean getAuto() {
        return auto.get();
    }
    
    ConversationPropagation getPropagation() {
        return (ConversationPropagation)propagationSource.get();
    }
   
}
