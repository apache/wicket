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

import java.util.List;

import org.apache.wicket.ThreadContext;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.time.Duration;


/**
 * Exposes Application related functionality for JMX.
 * 
 * @author eelcohillenius
 */
public class ResourceSettings implements ResourceSettingsMBean
{
	private final org.apache.wicket.Application application;

	/**
	 * Create.
	 * 
	 * @param application
	 */
	public ResourceSettings(final org.apache.wicket.Application application)
	{
		this.application = application;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLocalizer()
	{
		return Stringz.className(application.getResourceSettings().getLocalizer());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPackageResourceGuard()
	{
		return Stringz.className(application.getResourceSettings().getPackageResourceGuard());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPropertiesFactory()
	{
		ThreadContext.setApplication(application);

		try
		{
			return Stringz.className(application.getResourceSettings().getPropertiesFactory());
		}
		finally
		{
			ThreadContext.detach();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getResourceFinders()
	{
		StringBuilder builder = new StringBuilder();
		for (IResourceFinder rf : application.getResourceSettings().getResourceFinders())
		{
			builder.append(Stringz.className(rf));
		}
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getResourcePollFrequency()
	{
		Duration duration = application.getResourceSettings().getResourcePollFrequency();
		return (duration != null) ? duration.toString() : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getResourceStreamLocator()
	{
		return Stringz.className(application.getResourceSettings().getResourceStreamLocator());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getStringResourceLoaders()
	{
		List<IStringResourceLoader> loaders = application.getResourceSettings()
			.getStringResourceLoaders();
		if (loaders != null)
		{
			List<String> list = Generics.newArrayList();
			for (Object loader : loaders)
			{
				list.add(loader.toString());
			}
			return list.toArray(new String[loaders.size()]);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getThrowExceptionOnMissingResource()
	{
		return application.getResourceSettings().getThrowExceptionOnMissingResource();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getUseDefaultOnMissingResource()
	{
		return application.getResourceSettings().getUseDefaultOnMissingResource();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setThrowExceptionOnMissingResource(final boolean throwExceptionOnMissingResource)
	{
		application.getResourceSettings().setThrowExceptionOnMissingResource(
			throwExceptionOnMissingResource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUseDefaultOnMissingResource(final boolean useDefaultOnMissingResource)
	{
		application.getResourceSettings().setUseDefaultOnMissingResource(
			useDefaultOnMissingResource);
	}
}
