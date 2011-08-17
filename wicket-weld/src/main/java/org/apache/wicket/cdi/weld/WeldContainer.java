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
package org.apache.wicket.cdi.weld;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.wicket.Application;
import org.apache.wicket.cdi.CdiContainer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.jboss.weld.Container;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.http.HttpConversationContext;

/**
 * Weld specialization of {@link CdiContainer}
 * 
 * @author igor
 * 
 */
public class WeldContainer extends CdiContainer
{

	/**
	 * Constructor
	 * 
	 * @param beanManager
	 */
	public WeldContainer(BeanManager beanManager)
	{
		super(beanManager);
	}

	protected void bind(Application application)
	{
		super.bind(application);
	}

	@Override
	public void activateConversationalContext(RequestCycle cycle, String cid)
	{
		ConversationContext conversationContext = Container.instance()
			.deploymentManager()
			.instance()
			.select(Context.class)
			.select(HttpConversationContext.class)
			.get();
		conversationContext.activate(cid);
	}

	@Override
	public void deactivateConversationalContext(RequestCycle cycle)
	{
		ConversationContext conversationContext = Container.instance()
			.deploymentManager()
			.instance()
			.select(Context.class)
			.select(HttpConversationContext.class)
			.get();
		conversationContext.invalidate();
		conversationContext.deactivate();
	}

	@Override
	public Conversation getCurrentConversation(RequestCycle cycle)
	{
		ConversationContext conversationContext = Container.instance()
			.deploymentManager()
			.instance()
			.select(Context.class)
			.select(HttpConversationContext.class)
			.get();

		if (!conversationContext.isActive())
		{
			return null;
		}

		return conversationContext.getCurrentConversation();
	}

}
