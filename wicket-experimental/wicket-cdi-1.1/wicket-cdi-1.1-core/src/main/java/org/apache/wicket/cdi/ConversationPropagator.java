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


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.NonexistentConversationException;
import javax.inject.Inject;

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
 * @author igor
 */
@ApplicationScoped
public class ConversationPropagator extends AbstractRequestCycleListener
{

	private static final Logger logger = LoggerFactory.getLogger(ConversationPropagator.class);

	private static final MetaDataKey<String> CID_KEY = ConversationIdMetaKey.INSTANCE;

	static final String CID_ATTR = "cid";

	@Inject
	CdiConfiguration cdiConfiguration;

	@Inject
	AbstractCdiContainer container;

	@Inject
	ConversationManager conversationManager;


	private Conversation getConversation()
	{
		return container.getCurrentConversation();
	}

	@Override
	public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler)
	{
		Conversation conversation = getConversation();
		logger.debug("In onRequestHandlerResolved id = {}", conversation.getId());
		String cid = cycle.getRequest().getRequestParameters().getParameterValue(CID_ATTR).toString();
		Page page = getPage(handler);

		if (page == null)
		{
			return;
		}

		if (cid == null)
		{
			cid = page.getMetaData(CID_KEY);
		}


		if (cid != null && !conversation.isTransient() && !Objects.isEqual(conversation.getId(), cid))
		{
			logger.info("Conversation {} has expired for {}", cid, page);
			throw new ConversationExpiredException(null, cid, page, handler);
		}

		activateConversationIfNeeded(page, cycle, handler, cid);
	}

	@Override
	public IRequestHandler onException(RequestCycle cycle, Exception ex)
	{
		// if we are handling a stale page exception then use its conversation since we are most
		// likely about to rerender it.

		if (ex instanceof StalePageException)
		{
			IRequestablePage requestable = ((StalePageException) ex).getPage();
			if (requestable instanceof Page)
			{
				Page page = (Page) requestable;
				String cid = container.getConversationMarker(page);
				if (cid != null)
				{
					try
					{
						activateConversationIfNeeded(page, cycle, null, cid);
						return null;
					} catch (ConversationExpiredException e)
					{
						// ignore, we will start a new one below
					}
				}
			}
		}

		activateConversationIfNeeded(null, cycle, null, null);
		return null;
	}

	private void activateConversationIfNeeded(Page page, RequestCycle cycle, IRequestHandler handler,
	                                          String cid)
	{
		if (!activateForHandler(handler))
		{
			return;
		}

		try
		{
			Conversation conversation = getConversation();

			if (!(conversation.isTransient() && cid == null))
			{
				logger.debug("Activating conversation {}", cid);
				container.activateConversationalContext(cycle, cid);
			}

		} catch (NonexistentConversationException e)
		{
			logger.info("Unable to restore conversation with id {}", cid, e.getMessage());
			logger.debug("Unable to restore conversation", e);
			throw new ConversationExpiredException(e, cid, getPage(handler), handler);
		}

	}

	@Override
	public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler)
	{
		Conversation conversation = getConversation();
		logger.debug("In onRequestHandlerExecuted id = {}", conversation.getId());
		Page page = getPage(handler);

		if (page == null)
		{
			return;
		}

		if (autoEndIfNecessary(page, handler, conversation))
		{
			container.activateConversationalContext(cycle, null);
		} else
		{
			autoBeginIfNecessary(page, handler);
		}
		if (getPropagation().propagatesViaPage(page, handler))
		{
			// propagate a conversation across non-bookmarkable page instances
			setConversationOnPage(page);
		}
	}

	@Override
	public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler)
	{
		// propagate current non-transient conversation to the newly scheduled page

		Conversation conversation = getConversation();
		logger.debug("In onRequestHandlerScheduled id = {}", conversation.getId());
		if (conversation.isTransient())
		{
			return;
		}
		boolean propagated = false;
		Page page = getPage(handler);
		if (page != null)
		{
			if (getPropagation().propagatesViaPage(page, handler))
			{
				// propagate a conversation across non-bookmarkable page instances
				setConversationOnPage(page);
				propagated = true;
			}
		}

		if (getPropagation().propagatesViaParameters(handler))
		{
			// propagate cid to a scheduled bookmarkable page

			logger.debug(
					"Propagating non-transient conversation {} via page parameters of handler {}",
					conversation.getId(), handler);

			PageParameters parameters = getPageParameters(handler);
			if (parameters != null)
			{
				parameters.set(CID_ATTR, conversation.getId());
				propagated = true;
			}
		}
		if (!propagated && getAuto())
		{
			getConversationManager().scheduleConversationEnd();
		}
	}

	protected void setConversationOnPage(Page page)
	{
		Conversation conversation = getConversation();
		if (conversation.isTransient())
		{
			clearConversationOnPage(page);
		} else
		{

			logger.debug("Propagating non-transient conversation {} via meta of page instance {}",
					conversation.getId(), page);

			page.setMetaData(CID_KEY, conversation.getId());
		}
	}

	protected void clearConversationOnPage(Page page)
	{
		Conversation conversation = getConversation();
		logger.debug("Detaching transient conversation {} via meta of page instance {}",
				conversation.getId(), page);

		page.setMetaData(CID_KEY, null);
	}

	@Override
	public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url)
	{
		Conversation conversation = getConversation();
		logger.debug("In onUrlMapped id = {}", conversation.getId());
		// no need to propagate the conversation to packaged resources, they should never change
		if (handler instanceof ResourceReferenceRequestHandler)
		{
			if (((ResourceReferenceRequestHandler) handler).getResourceReference() instanceof PackageResourceReference)
			{
				return;
			}
		}


		if (conversation.isTransient())
		{
			return;
		}

		if (getPropagation().propagatesViaParameters(handler))
		{
			// propagate cid to bookmarkable pages via urls

			logger.debug("Propagating non-transient conversation {} via url", conversation.getId());

			url.setQueryParameter(CID_ATTR, conversation.getId());
		} else
		{
			//we did not propagate.
			//Cancel scheduled conversation end if page is auto.
			Page page = getPage(handler);
			if (page != null)
			{
				Conversational annotation = page.getClass().getAnnotation(Conversational.class);
				if (annotation != null)
				{
					if (annotation.auto() && getConversationManager().isConversationScheduledForEnd())
					{
						getConversationManager().cancelConversationEnd(); //was scheduled to end but next page is auto
					}
				}
			}
		}
	}

	/**
	 * Determines whether or not a conversation should be activated for the specified handler. This
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

	protected void autoBeginIfNecessary(Page page, IRequestHandler handler)
	{

		if (page == null)
		{
			return;
		}

		Conversational annotation = page.getClass().getAnnotation(Conversational.class);

		boolean auto = getAuto();
		auto |= annotation == null ? false : annotation.auto();

		// possibly changing propagation and auto is not set
		if (annotation == null && !auto)
		{
			return;
		}

		if (getConversation().isTransient())
		{
			if (auto)
			{
				getConversation().begin();
				logger.debug("Auto-began conversation {} for page {}", getConversation().getId(), page);
			}
		}
		ConversationPropagation prop = annotation != null ? annotation.prop() : getPropagation();
		// The conversationManager is attached to a conversation so update
		if (!getConversation().isTransient())
		{
			getConversationManager().setPropagation(prop);
			getConversationManager().setManageConversation(auto);
		} else
		{
			if (prop != getPropagation())
			{
				logger.info("Not setting propagation to {} because no conversation is started.",
						prop);
			}
		}
	}

	protected boolean autoEndIfNecessary(Page page, IRequestHandler handler, Conversation conversation)
	{
		if (page == null || conversation.isTransient())
		{
			return false;
		}

		boolean endConversation = getConversationManager().isConversationScheduledForEnd();
		if (endConversation)
		{
			String cid = conversation.getId();
			getConversation().end();
			logger.debug("Auto-ended conversation {} for page {}", cid, page);
		}
		return endConversation;
	}


	// Currently not being used will reinvestigate this concept.
	protected boolean hasConversationalComponent(Page page)
	{
		Boolean hasConversational = Visits.visit(page, new IVisitor<Component, Boolean>()
		{
			@Override
			public void component(Component object, IVisit<Boolean> visit)
			{
				Conversational annotation = object.getClass().getAnnotation(Conversational.class);
				if (annotation != null)
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
			handler = ((IRequestHandlerDelegate) handler).getDelegateHandler();
		}

		if (handler instanceof IPageRequestHandler)
		{
			IPageRequestHandler pageHandler = (IPageRequestHandler) handler;
			if (pageHandler.isPageInstanceCreated())
			{
				return (Page) pageHandler.getPage();
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
			IPageClassRequestHandler pageHandler = (IPageClassRequestHandler) handler;
			return pageHandler.getPageParameters();
		}
		return null;
	}

	ConversationManager getConversationManager()
	{
		if (getConversation().isTransient())
		{
			logger.warn("Accessing Conversation Manager from transient Conversation Context");
		}
		return conversationManager;
	}

	Boolean getAuto()
	{
		if (getConversation().isTransient())
		{
			logger.debug("Getting Global Auto setting");
			return cdiConfiguration.isAutoConversationManagement();
		}
		logger.debug("Getting Auto setting for conversation = {}", getConversation().getId());
		return getConversationManager().getManageConversation();
	}

	ConversationPropagation getPropagation()
	{
		if (getConversation().isTransient())
		{
			logger.debug("Getting global Propagation {}.", cdiConfiguration.getPropagation());
			return (ConversationPropagation) cdiConfiguration.getPropagation();
		}
		logger.debug("Propagation is set to {} with id = {}", getConversationManager().getPropagation(), getConversation().getId());
		return (ConversationPropagation) getConversationManager().getPropagation();
	}

}
