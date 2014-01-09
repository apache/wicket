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
package org.apache.wicket.spring;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.lang.Packages;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link SpringWebApplicationFactory}.
 * 
 * @author svenmeier
 */
public class SpringWebApplicationFactoryTest extends Assert
{

	/**
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception
	{
		WicketFilter filter = new WicketFilter();

		filter.init(new FilterConfigImpl());

		assertFalse(Destroyable.instance.destroyed);

		filter.destroy();

		assertTrue("is not destroyed", Destroyable.instance.destroyed);
	}

	private class FilterConfigImpl implements FilterConfig
	{

		@Override
		public String getFilterName()
		{
			return "test";
		}

		@Override
		public ServletContext getServletContext()
		{
			return new MockServletContext(null, null);
		}

		@Override
		public String getInitParameter(String name)
		{
			if ("applicationFactoryClassName".equals(name))
			{
				// use Spring factory
				return SpringWebApplicationFactory.class.getName();
			}
			if ("contextConfigLocation".equals(name))
			{
				// use application-specific context
				return "classpath:" + Packages.absolutePath(getClass(), "applicationContext.xml");
			}
			return null;
		}

		@Override
		public Enumeration<String> getInitParameterNames()
		{
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Application configured in the application context.
	 */
	public static class Application extends WebApplication
	{
		@Override
		public Class<? extends Page> getHomePage()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		protected void init()
		{
			super.init();

			try
			{
				new SpringComponentInjector(this);
			}
			catch (Exception ex)
			{
				fail("does not work with application-specific context");
			}
		}
	}

	/**
	 * A destroyable bean defined in the application context.
	 */
	public static class Destroyable
	{
		static Destroyable instance;

		boolean destroyed;

		/**
		 */
		public Destroyable()
		{
			instance = this;
		}

		/**
		 * Called by Spring.
		 */
		public void destroy()
		{
			destroyed = true;
		}
	}
}
