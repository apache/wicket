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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	public ResourceSettings(org.apache.wicket.Application application)
	{
		this.application = application;
	}

	/**
	 * @see org.apache.wicket.jmx.ResourceSettingsMBean#getLocalizer()
	 */
	public String getLocalizer()
	{
		return Stringz.className(application.getResourceSettings().getLocalizer());
	}

	/**
	 * @see org.apache.wicket.jmx.ResourceSettingsMBean#getPackageResourceGuard()
	 */
	public String getPackageResourceGuard()
	{
		return Stringz.className(application.getResourceSettings().getPackageResourceGuard());
	}

	/**
	 * @see org.apache.wicket.jmx.ResourceSettingsMBean#getPropertiesFactory()
	 */
	public String getPropertiesFactory()
	{
		return Stringz.className(application.getResourceSettings().getPropertiesFactory());
	}

	/**
	 * @see org.apache.wicket.jmx.ResourceSettingsMBean#getResourceFinder()
	 */
	public String getResourceFinder()
	{
		return Stringz.className(application.getResourceSettings().getResourceFinder());
	}

	/**
	 * @see org.apache.wicket.jmx.ResourceSettingsMBean#getResourcePollFrequency()
	 */
	public String getResourcePollFrequency()
	{
		Duration duration = application.getResourceSettings().getResourcePollFrequency();
		return (duration != null) ? duration.toString() : null;
	}

	/**
	 * @see org.apache.wicket.jmx.ResourceSettingsMBean#getResourceStreamLocator()
	 */
	public String getResourceStreamLocator()
	{
		return Stringz.className(application.getResourceSettings().getResourceStreamLocator());
	}

	/**
	 * @see org.apache.wicket.jmx.ResourceSettingsMBean#getStringResourceLoaders()
	 */
	@SuppressWarnings("unchecked")
	public String[] getStringResourceLoaders()
	{
		List loaders = application.getResourceSettings().getStringResourceLoaders();
		if (loaders != null)
		{
			List list = new ArrayList();
			for (Iterator iter = loaders.iterator(); iter.hasNext();)
			{
				list.add(iter.next().toString());
			}
			return (String[])list.toArray(new String[loaders.size()]);
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.jmx.ResourceSettingsMBean#getThrowExceptionOnMissingResource()
	 */
	public boolean getThrowExceptionOnMissingResource()
	{
		return application.getResourceSettings().getThrowExceptionOnMissingResource();
	}

	/**
	 * @see org.apache.wicket.jmx.ResourceSettingsMBean#getUseDefaultOnMissingResource()
	 */
	public boolean getUseDefaultOnMissingResource()
	{
		return application.getResourceSettings().getUseDefaultOnMissingResource();
	}

	/**
	 * @see org.apache.wicket.jmx.ResourceSettingsMBean#setThrowExceptionOnMissingResource(boolean)
	 */
	public void setThrowExceptionOnMissingResource(boolean throwExceptionOnMissingResource)
	{
		application.getResourceSettings().setThrowExceptionOnMissingResource(
				throwExceptionOnMissingResource);
	}

	/**
	 * @see org.apache.wicket.jmx.ResourceSettingsMBean#setUseDefaultOnMissingResource(boolean)
	 */
	public void setUseDefaultOnMissingResource(boolean useDefaultOnMissingResource)
	{
		application.getResourceSettings().setUseDefaultOnMissingResource(
				useDefaultOnMissingResource);
	}
}
