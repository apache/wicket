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
package org.apache.wicket.cdi.util.tester;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.context.http.HttpConversationContext;
import org.jboss.weld.context.http.HttpRequestContext;
import org.jboss.weld.context.http.HttpSessionContext;
import org.jglue.cdiunit.internal.SessionHolderAwareRequest;

/**
 * @author jsarman
 */
@ApplicationScoped
public class ContextManager
{

	@Inject
	private HttpRequestContext requestContext;
	@Inject
	private HttpSessionContext sessionContext;
	@Inject
	private HttpConversationContext conversationContext;

	public void activateContexts(HttpServletRequest request)
	{
		requestContext.associate(new SessionHolderAwareRequest(request));
		if (!requestContext.isActive())
		{
			requestContext.activate();
		}

		sessionContext.associate(new SessionHolderAwareRequest(request));
		if (!sessionContext.isActive())
		{
			sessionContext.activate();
		}

		conversationContext.associate(new SessionHolderAwareRequest(request));
		if (!conversationContext.isActive())
		{
			String cid = request.getParameter("cid");
			conversationContext.activate(cid);
		}
	}

	public void deactivateContexts(HttpServletRequest request)
	{
		requestContext.associate(new SessionHolderAwareRequest(request));
		if (requestContext.isActive())
		{
			requestContext.invalidate();
			requestContext.deactivate();
		}

		sessionContext.associate(new SessionHolderAwareRequest(request));
		if (sessionContext.isActive())
		{
			sessionContext.invalidate();
			sessionContext.deactivate();
		}

		conversationContext.associate(new SessionHolderAwareRequest(request));
		if (conversationContext.isActive())
		{
			conversationContext.invalidate();
			conversationContext.deactivate();
		}
	}

	public void destroy(HttpSession session)
	{
		conversationContext.destroy(session);
		sessionContext.destroy(session);
	}
}
