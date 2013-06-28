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
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jboss.weld.context.http.HttpConversationContext;

/**
 * @author jsarman
 */
@ApplicationScoped
public class MockContainer extends AbstractCdiContainer
{

	@Inject
	private Instance<HttpConversationContext> conversationContextSource;

	@Override
	public void deactivateConversationalContext(RequestCycle cycle)
	{
		HttpConversationContext conversationContext = conversationContextSource.get();
		conversationContext.deactivate();
		conversationContext.dissociate(getRequest(cycle));
	}

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
		HttpConversationContext conversationContext = conversationContextSource.get();
		conversationContext.associate(getRequest(cycle));
		if (conversationContext.isActive())
		{
			// Only reactivate if transient and cid is set
			if (conversationContext.getCurrentConversation().isTransient()
					&& cid != null && !cid.isEmpty())
			{
				conversationContext.deactivate();
				conversationContext.activate(cid);
			}
		} else
		{
			conversationContext.activate(cid);
		}
	}
}
