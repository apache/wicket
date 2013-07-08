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
import javax.inject.Inject;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jboss.weld.context.http.HttpConversationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jsarman
 */
@ApplicationScoped
public class MockCdiContainer extends AbstractCdiContainer
{
	private static final Logger logger = LoggerFactory.getLogger(MockCdiContainer.class);

	@Inject
	private HttpConversationContext conversationContext;

	/**
	 * Activates the conversational context and starts the conversation with the
	 * specified cid
	 *
	 * @param cycle
	 * @param cid
	 */
	@Override
	public void activateConversationalContext(RequestCycle cycle, String cid)
	{
		conversationContext.associate(getRequest(cycle));
		if (conversationContext.isActive())
		{
			conversationContext.invalidate();
			conversationContext.deactivate();
			conversationContext.activate(cid);
		} else
		{
			conversationContext.activate(cid);
		}
	}

	@Override
	public Conversation getCurrentConversation()
	{
		return conversationContext.getCurrentConversation();
	}

}
