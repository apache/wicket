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
package org.apache.wicket.guice;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import com.google.inject.AbstractModule;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.junit.Test;

/**
 */
public class GuiceWebApplicationFactoryTest
{
	/**
	 * testWebAppCreation()
	 */
	@Test
	public void testWebAppCreation()
	{
		new GuiceWebApplicationFactory().createApplication(createFilter());
	}

	/**
	 */
	public static class TestModule extends AbstractModule
	{

		@Override
		protected void configure()
		{
			bind(WebApplication.class).toInstance(new WebApplication()
			{
				@Override
				public Class<? extends Page> getHomePage()
				{
					return null;
				}
			});
		}
	}


	private WicketFilter createFilter()
	{
		return new WicketFilter()
		{
			@Override
			public FilterConfig getFilterConfig()
			{

				return new FilterConfig()
				{
					@Override
					public String getInitParameter(final String param)
					{
						if ("module".equals(param))
						{
							return TestModule.class.getName();
						}
						return null;
					}

					@Override
					public ServletContext getServletContext()
					{
						return new MockServletContext(null, null);
					}

					@Override
					public Enumeration<String> getInitParameterNames()
					{
						return null;
					}

					@Override
					public String getFilterName()
					{
						return null;
					}
				};
			}

		};
	}
}
