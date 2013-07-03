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

import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.FilterConfig;

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.jglue.cdiunit.AdditionalClasses;
import org.mockito.Mockito;

/**
 * @author jsarman
 */
@AdditionalClasses({
		CdiWicketFilter.class,
		CdiWebApplicationFactory.class
})
public abstract class WicketCdiFilterBaseTest extends WicketCdiTestCase
{

	@Inject
	CdiWicketFilter cdiWicketFilter;

	Application testFilterInitialization(ConfigurationParameters params, String appName)
	{
		String testAppKey = "testKey " + UUID.randomUUID();

		//The factory was injected into the filter
		IWebApplicationFactory factory = cdiWicketFilter.getApplicationFactory();

		//build a fake filter so the factory can get a mocked filter config
		WicketFilter fakeFilter = Mockito.mock(WicketFilter.class);
		FilterConfig fc = Mockito.mock(FilterConfig.class);
		if (appName != null)
		{
			when(fc.getInitParameter(CdiWebApplicationFactory.WICKET_APP_NAME))
					.thenReturn(appName);
		}
		if (params != null)
		{
			buildParams(params, fc);
		}
		when(fakeFilter.getFilterConfig()).thenReturn(fc);
		when(fc.getFilterName()).thenReturn(testAppKey);

		//Create the app
		WebApplication app = factory.createApplication(fakeFilter);
		ThreadContext.detach();
		app.setName(testAppKey); //This is normally done in the filter but not testing that

		ThreadContext.setApplication(app);
		//Did we configure
		assertTrue(CdiConfiguration.get().isConfigured());

		return app;
	}

	private void buildParams(ConfigurationParameters params, FilterConfig fc)
	{
		when(fc.getInitParameter(CdiWebApplicationFactory.AUTO_CONVERSATION))
				.thenReturn(Boolean.toString(params.isAutoConversationManagement()));
		when(fc.getInitParameter(CdiWebApplicationFactory.INJECT_APP))
				.thenReturn(Boolean.toString(params.isInjectApplication()));
		when(fc.getInitParameter(CdiWebApplicationFactory.INJECT_BEHAVIOR))
				.thenReturn(Boolean.toString(params.isInjectBehaviors()));
		when(fc.getInitParameter(CdiWebApplicationFactory.INJECT_COMPONENT))
				.thenReturn(Boolean.toString(params.isInjectComponents()));
		when(fc.getInitParameter(CdiWebApplicationFactory.INJECT_SESSION))
				.thenReturn(Boolean.toString(params.isInjectSession()));
		when(fc.getInitParameter(CdiWebApplicationFactory.PROPAGATION))
				.thenReturn(params.getPropagation().toString());
	}
}
