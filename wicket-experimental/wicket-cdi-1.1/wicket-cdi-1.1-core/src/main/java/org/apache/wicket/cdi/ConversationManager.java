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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jsarman
 */
@ConversationScoped
public class ConversationManager implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(ConversationManager.class);

	@Inject
	AbstractCdiContainer container;

	@Inject
	@Auto
	Boolean globalAuto;

	@Inject
	@Propagation
	IConversationPropagation globalPropagation;

	IConversationPropagation propagation;
	Boolean manageConversation;

	boolean terminateConversation;

	@PostConstruct
	public void init()
	{
		logger.debug("Starting new Conversation manager for id = {}", getConversation().getId());
		propagation = globalPropagation;
		manageConversation = globalAuto;
		logger.debug("Setting initial values to auto = {} prop = {}", manageConversation,
				propagation);
	}

	private Conversation getConversation()
	{
		return container.getCurrentConversation();
	}

	public IConversationPropagation getPropagation()
	{
		return propagation;
	}

	public void setPropagation(IConversationPropagation propagation)
	{
		if (propagation == null)
		{
			throw new IllegalArgumentException("Propagation cannot be null");
		}
		if (this.propagation == propagation)
		{
			return;
		}

		logger.debug("Changing conversation dependent propagation to {} for id = {}", propagation,
				getConversation().getId());

		this.propagation = propagation;
	}

	public Boolean getManageConversation()
	{
		return manageConversation;
	}

	public void setManageConversation(boolean manageConversation)
	{
		if (this.manageConversation == manageConversation)
		{
			return;
		}
		logger.debug("Setting conversation dependent manageConversation to {} for id = {} ",
				manageConversation, getConversation().getId());

		this.manageConversation = manageConversation;
	}

	void cancelConversationEnd()
	{
		terminateConversation = false;
	}

	void scheduleConversationEnd()
	{
		terminateConversation = true;
	}

	boolean isConversationScheduledForEnd()
	{
		return terminateConversation;
	}

}
