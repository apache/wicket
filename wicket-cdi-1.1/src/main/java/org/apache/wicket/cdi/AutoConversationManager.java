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
import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.application.IComponentOnBeforeRenderListener;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.util.visit.Visits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automatically starts and ends conversations for pages with a
 * {@link ConversationalComponent}.
 * 
 * @author papegaaij
 */
public class AutoConversationManager implements IComponentOnBeforeRenderListener
{
	private static final Logger logger = LoggerFactory.getLogger(AutoConversationManager.class);

	@Inject
	private Conversation conversation;

	@Inject
	private AutoConversation autoConversation;

	private IConversationPropagation propagation;

	public AutoConversationManager(IConversationPropagation propagation)
	{
		NonContextual.of(AutoConversationManager.class).inject(this);
		this.propagation = propagation;
	}

	@Override
	public void onBeforeRender(Component component)
	{
		if (component instanceof Page)
		{
			Page page = (Page)component;
			IRequestHandler activeRequestHandler = page.getRequestCycle().getActiveRequestHandler();
			autoEndIfNecessary(page, activeRequestHandler);
			autoBeginIfNecessary(page, activeRequestHandler);
		}
	}

	protected void autoBeginIfNecessary(Page page, IRequestHandler handler)
	{
		if (conversation == null || !conversation.isTransient() || page == null
				|| !hasConversationalComponent(page) || !propagation.propagatesVia(handler, page))
		{
			return;
		}

		// auto activate conversation

		conversation.begin();
		autoConversation.setAutomatic(true);

		logger.debug("Auto-began conversation '{}' for page '{}'", conversation.getId(), page);
	}

	protected void autoEndIfNecessary(Page page, IRequestHandler handler)
	{
		if (conversation == null || conversation.isTransient() || page == null
				|| hasConversationalComponent(page) || !propagation.propagatesVia(handler, page)
				|| autoConversation.isAutomatic() == false)
		{
			return;
		}

		// auto de-activate conversation

		String cid = conversation.getId();

		autoConversation.setAutomatic(false);
		conversation.end();
		ConversationPropagator.removeConversationIdFromPage(page);

		logger.debug("Auto-ended conversation '{}' for page '{}'", cid, page);
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
}
