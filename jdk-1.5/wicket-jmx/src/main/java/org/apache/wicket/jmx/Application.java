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

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestCodingStrategy;
import org.apache.wicket.request.IRequestTargetMountsInfo;
import org.apache.wicket.request.target.coding.IMountableRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy;


/**
 * Exposes Application related functionality for JMX.
 * 
 * @author eelcohillenius
 */
public class Application implements ApplicationMBean
{
	private final org.apache.wicket.Application application;

	private final WebApplication webApplication;

	/**
	 * Create.
	 * 
	 * @param application
	 */
	public Application(org.apache.wicket.Application application)
	{
		this.application = application;

		// do this so that we don't have to cast all the time
		if (application instanceof WebApplication)
		{
			this.webApplication = (WebApplication)application;
		}
		else
		{
			this.webApplication = null;
		}
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationMBean#clearMarkupCache()
	 */
	public void clearMarkupCache() throws IOException
	{
		application.getMarkupSettings().getMarkupCache().clear();
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationMBean#getApplicationClass()
	 */
	public String getApplicationClass() throws IOException
	{
		return application.getClass().getName();
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationMBean#getConfigurationType()
	 */
	public String getConfigurationType()
	{
		return application.getConfigurationType();
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationMBean#getHomePageClass()
	 */
	public String getHomePageClass() throws IOException
	{
		return application.getHomePage().getName();
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationMBean#getMarkupCacheSize()
	 */
	public int getMarkupCacheSize() throws IOException
	{
		return application.getMarkupSettings().getMarkupCache().size();
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationMBean#getMounts()
	 */
	public String[] getMounts() throws IOException
	{
		if (webApplication != null)
		{
			IRequestCodingStrategy mounter = webApplication.getRequestCycleProcessor()
					.getRequestCodingStrategy();
			if (mounter instanceof IRequestTargetMountsInfo)
			{
				IRequestTargetMountsInfo mountsInfo = (IRequestTargetMountsInfo)mounter;
				IRequestTargetUrlCodingStrategy[] targets = mountsInfo.listMounts();
				String[] results = new String[targets.length];
				for (int i = 0; i < targets.length; i++)
				{
					if (targets[i] instanceof IMountableRequestTargetUrlCodingStrategy)
					{
						// ugly hack for 1.2 to avoid breaking the API
						results[i] = ((IMountableRequestTargetUrlCodingStrategy)targets[i])
								.getMountPath() +
								" - " + targets[i].toString();
					}
					else
					{
						// should never happen, but to be sure
						results[i] = "/? - " + targets[i].toString();
					}
				}
				return results;
			}
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.jmx.ApplicationMBean#getWicketVersion()
	 */
	public String getWicketVersion() throws IOException
	{
		return application.getFrameworkSettings().getVersion();
	}
}
