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

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.servlet.FilterConfig;

import org.apache.wicket.cdi.CdiWebApplicationFactory;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.util.tester.WicketTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jsarman
 */
@Dependent
public class CdiWicketTester extends WicketTester
{

	private static final Logger logger = LoggerFactory.getLogger(CdiWicketTester.class);
	@Inject
	ContextManager contextManager;

	TestCdiConfiguration cdiConfiguration;
	FilterConfig filterConfig;


	@Inject
	public CdiWicketTester(TestCdiConfiguration cdiConfiguration, CdiWebApplicationFactory factory, @ConfigurationFilter final FilterConfig filterConfig)
	{
		super(factory.createApplication(new WicketFilter()
		{

			@Override
			public FilterConfig getFilterConfig()
			{
				return filterConfig;
			}

		}));
		this.cdiConfiguration = cdiConfiguration;
		this.filterConfig = filterConfig;
	}

	@PostConstruct
	public void initializeApp()
	{
		logger.debug("Initialized Cdi Wicket Tester");
		cdiConfiguration.remapApplicationKey(filterConfig.getFilterName(), getApplication());
		contextManager.activateContexts(getRequest()); //Start up contexts in case no requests are performed
	}


	public void configure()
	{
		if (!cdiConfiguration.isConfigured())
		{
			cdiConfiguration.configure(getApplication());
		}
	}

	private AtomicInteger count = new AtomicInteger();

	/**
	 * Process the request by first activating the contexts on initial call.
	 * This call is called recursively in the super class so keep track of
	 * the topmost call and only activate and deactivate the contexts during that time.
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
		if (count.getAndIncrement() == 0)
		{

			if (getLastRequest() != null)
			{
				contextManager.deactivateContexts(getLastRequest());
			} else
			{
				configure();//make sure we are configured for cdi
			}
			contextManager.activateContexts(getRequest());
		}
		try
		{
			return super.processRequest(forcedRequest, forcedRequestHandler, redirect);
		} finally
		{
			count.decrementAndGet();
		}
	}

	@PreDestroy
	public void finish()
	{
		try
		{
			logger.debug("Destroying Cdi Wicket Tester");
			if (getLastRequest() != null)
			{
				contextManager.deactivateContexts(getLastRequest());
			}
			contextManager.destroy(getHttpSession());
			destroy();
		} catch (Throwable t)
		{
		}
	}
}
