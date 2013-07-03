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
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jsarman
 */
@ConversationScoped
public class ConversationManager implements Serializable
{
	private static final Logger logger = LoggerFactory.getLogger(ConversationManager.class);
	@Inject
	Conversation conversation;

	@Inject
	@Propagation
	Instance<IConversationPropagation> propagations;

	@Inject
	@Auto
	Instance<Boolean> manageConversations;
	IConversationPropagation propagation;
	Boolean manageConversation;

	boolean containerManaged;

	public IConversationPropagation getPropagation()
	{
		return propagation == null ? propagations.get() : propagation;
	}

	public void setPropagation(IConversationPropagation propagation)
	{
		if (propagation == null)
		{
			throw new IllegalArgumentException("Propagation cannot be null");
		}
		if (conversation.isTransient())
		{
			logger.warn("Attempt to set Propagation with transient conversation. Ignoring.");
			return;
		}
		if (this.propagation == propagation)
		{
			return;
		}
		if (propagation == ConversationPropagation.NONE)
		{
			logger.warn("Changing conversation dependent propagation to NONE can cause undesirable results.");
		} else
		{
			logger.debug("Changing conversation dependent propagation to {} for id = {}",
					propagation, conversation.getId());
		}
		this.propagation = propagation;
	}

	public Boolean getManageConversation()
	{
		return manageConversation == null ? manageConversations.get() : manageConversation;
	}

	public void setManageConversation(boolean manageConversation)
	{
		if (conversation.isTransient())
		{
			logger.warn("Attempt to set manageConversation with transient conversation. Ignoring.");
			return;
		}
		logger.debug("Setting conversation dependent manageConversation to {} for id = {} ",
				manageConversation, conversation.getId());
		this.manageConversation = manageConversation;
	}

	boolean getContainerManaged()
	{
		return containerManaged;
	}

	void setContainerManaged(boolean managed, IConversationPropagation propagation)
	{
		setManageConversation(managed);
		setPropagation(propagation);
		containerManaged = managed;
	}


	public void endConversation()
	{
		conversation.end();
	}

}
