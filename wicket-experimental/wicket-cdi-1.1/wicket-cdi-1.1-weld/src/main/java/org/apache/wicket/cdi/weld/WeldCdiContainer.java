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

import javax.enterprise.context.ApplicationScoped;
import org.apache.wicket.cdi.*;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.apache.wicket.request.cycle.RequestCycle;
import org.jboss.weld.context.http.HttpConversationContext;

/**
 * Provides access to CDI features from inside a Wicket request
 * 
 * @author jsarman
 * 
 */
@ApplicationScoped
public class WeldCdiContainer extends AbstractCdiContainer
{
	@Inject 
	Instance<HttpConversationContext> conversationContextSource;
       
	@Inject
	INonContextualManager nonContextualManager;
	
	/**
	 * Constructor	 	 
	 */
	public WeldCdiContainer()
	{

	}

	@Override
	protected INonContextualManager getNonContextualManager()
	{
		return nonContextualManager;
	}

	/**
	 * Deactivates conversational context
	 * 
	 * @param cycle
	 */
	@Override
	public void deactivateConversationalContext(RequestCycle cycle)
	{	    
		HttpConversationContext conversationContext = conversationContextSource.get(); 
		conversationContext.deactivate();	            
		conversationContext.dissociate(getRequest(cycle));
	}

	/**
	 * Activates the conversational context and starts the conversation with the specified cid
	 * 
	 * @param cycle
	 * @param cid
	 */
	@Override
	public void activateConversationalContext(RequestCycle cycle, String cid)
	{
		// Force a session created if one does not exist 
		// Glassfish does not have a session initially to store the transactions
		// so it gets lost in the request.              
		getRequest(cycle).getSession(true);
		HttpConversationContext conversationContext = conversationContextSource.get();               
		conversationContext.associate(getRequest(cycle)); 
		if(conversationContext.isActive())
		{
			// Only reactivate if transient and cid is set
			if(conversationContext.getCurrentConversation().isTransient() 
				&& cid != null && !cid.isEmpty())
			{
				conversationContext.deactivate();
				conversationContext.activate(cid);                                                             
			}                
		} 
		else
		{
			conversationContext.activate(cid);         
		}
	}
}
