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
package org.apache.wicket.jmx;

import java.io.IOException;

import org.apache.wicket.ThreadContext;

/**
 * Exposes Application related functionality for JMX.
 * 
 * @author eelcohillenius
 */
public class Application implements ApplicationMBean
{
	private final org.apache.wicket.Application application;

	/**
	 * Create.
	 * 
	 * @param application
	 */
	public Application(final org.apache.wicket.Application application)
	{
		this.application = application;
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationMBean#clearMarkupCache()
	 */
	@Override
	public void clearMarkupCache() throws IOException
	{
		application.getMarkupSettings().getMarkupFactory().getMarkupCache().clear();
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationMBean#getApplicationClass()
	 */
	@Override
	public String getApplicationClass() throws IOException
	{
		return application.getClass().getName();
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationMBean#getConfigurationType()
	 */
	@Override
	public String getConfigurationType()
	{
		return application.getConfigurationType().name();
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationMBean#getHomePageClass()
	 */
	@Override
	public String getHomePageClass() throws IOException
	{
		return application.getHomePage().getName();
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationMBean#getMarkupCacheSize()
	 */
	@Override
	public int getMarkupCacheSize() throws IOException
	{
		ThreadContext.setApplication(application);
		
		try
		{
			return application.getMarkupSettings().getMarkupFactory().getMarkupCache().size();
		}
		finally
		{
			ThreadContext.detach();
		}
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationMBean#getWicketVersion()
	 */
	@Override
	public String getWicketVersion() throws IOException
	{
		return application.getFrameworkSettings().getVersion();
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationMBean#clearLocalizerCache()
	 */
	@Override
	public void clearLocalizerCache() throws IOException
	{
		application.getResourceSettings().getLocalizer().clearCache();
	}
}
