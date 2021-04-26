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

import jakarta.enterprise.context.Conversation;
import jakarta.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.application.IComponentOnBeforeRenderListener;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks for conversation expiration during page render and throws a
 * {@link ConversationExpiredException} when an expired conversation is
 * detected.
 * 
 * For example a link that calls {@link Conversation#end()} but does not
 * redirect to a non-conversation-dependent page will be caught by this
 * listener.
 * 
 * @author igor
 * 
 */
public class ConversationExpiryChecker implements IComponentOnBeforeRenderListener
{
	private static final Logger logger = LoggerFactory.getLogger(ConversationExpiryChecker.class);

	@Inject
	private Conversation conversation;

	public ConversationExpiryChecker()
	{
		NonContextual.of(ConversationExpiryChecker.class).inject(this);
	}

	@Override
	public void onBeforeRender(Component component)
	{
		if (component instanceof Page || RequestCycle.get().find(IPartialPageRequestHandler.class).isPresent())
		{
			Page page = component.getPage();
			String cid = ConversationPropagator.getConversationIdFromPage(page);
			if (cid != null && !Objects.isEqual(conversation.getId(), cid))
			{
				logger.info("Conversation {} has expired for {}", cid, page);
				throw new ConversationExpiredException(null, cid, page, RequestCycle.get()
						.getActiveRequestHandler());
			}
		}
	}
}
