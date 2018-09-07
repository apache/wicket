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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.WicketTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jsarman
 */
public class CdiWicketTester extends WicketTester
{
	private static final Pattern COUNT_PATTERN = Pattern.compile("COUNT=x([0-9]+)x");
	private static final Logger logger = LoggerFactory.getLogger(CdiWicketTester.class);

	@Inject
	ContextManager contextManager;

	public CdiWicketTester(WebApplication app)
	{
		super(app);
		NonContextual.of(CdiWicketTester.class).inject(this);
		getHttpSession().setTemporary(false);
	}

	/**
	 * Process the request by first activating the contexts on initial call. This call is called
	 * recursively in the super class so keep track of the topmost call and only activate and
	 * deactivate the contexts during that time.
	 * 
	 * @param forcedRequest
	 * @param forcedRequestHandler
	 * @param redirect
	 * @return
	 */
	@Override
	protected boolean processRequest(final MockHttpServletRequest forcedRequest,
		final IRequestHandler forcedRequestHandler, final boolean redirect)
	{
		if (getLastRequest() != null)
		{
			contextManager.deactivateContexts();
		}
		contextManager.activateContexts(forcedRequest == null ? getRequest() : forcedRequest);
		return super.processRequest(forcedRequest, forcedRequestHandler, redirect);
	}

	@Override
	public Url urlFor(IRequestHandler handler)
	{
		Url ret = super.urlFor(handler);
		final CdiConfiguration configuration = CdiConfiguration.get(getApplication());
		Page page = ConversationPropagator.getPage(handler);
		if (configuration.getPropagation().propagatesVia(handler, page))
		{
			if (page != null)
			{
				String cid = ConversationPropagator.getConversationIdFromPage(page);
				ret.addQueryParameter(ConversationPropagator.CID, cid);
			}
		}
		return ret;
	}

	@PreDestroy
	public void finish()
	{
		try
		{
			logger.debug("Destroying Cdi Wicket Tester");
			if (getLastRequest() != null)
			{
				contextManager.deactivateContexts();
			}
			contextManager.destroy();
			destroy();
		}
		catch (Throwable t)
		{
		}
	}

	/**
	 * Asserts that the response contains the right count. This can only be done by parsing the
	 * markup because models only contain valid values during a request, not after.
	 * 
	 * @param count
	 *            TODO
	 */
	public void assertCount(int count)
	{
		assertTrue(getLastResponseAsString().contains("COUNT=x"),
			"Response does not contain a count");
		Matcher matcher = COUNT_PATTERN.matcher(getLastResponseAsString());
		assertTrue(matcher.find());
		assertEquals(Integer.toString(count), matcher.group(1));
	}
}
