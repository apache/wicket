/*
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.jmx;

import java.io.IOException;

import wicket.protocol.http.WebApplication;
import wicket.request.IRequestCodingStrategy;
import wicket.request.IRequestTargetMountsInfo;
import wicket.request.target.coding.IRequestTargetUrlCodingStrategy;

/**
 * Exposes Application related functionality for JMX.
 * 
 * @author eelcohillenius
 */
public class Application implements ApplicationMBean
{
	private final wicket.Application application;

	private final WebApplication webApplication;

	/**
	 * Create.
	 * 
	 * @param application
	 */
	public Application(wicket.Application application)
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
	 * @see wicket.jmx.ApplicationMBean#clearMarkupCache()
	 */
	public void clearMarkupCache() throws IOException
	{
		application.getMarkupCache().clear();
	}

	/**
	 * @see wicket.jmx.ApplicationMBean#getApplicationClass()
	 */
	public String getApplicationClass() throws IOException
	{
		return application.getClass().getName();
	}

	/**
	 * @see wicket.jmx.ApplicationMBean#getConfigurationType()
	 */
	public String getConfigurationType()
	{
		return application.getConfigurationType();
	}

	/**
	 * @see wicket.jmx.ApplicationMBean#getHomePageClass()
	 */
	public String getHomePageClass() throws IOException
	{
		return application.getClass().getName();
	}

	/**
	 * @see wicket.jmx.ApplicationMBean#getMarkupCacheSize()
	 */
	public int getMarkupCacheSize() throws IOException
	{
		return application.getMarkupCache().size();
	}

	/**
	 * @see wicket.jmx.ApplicationMBean#getMounts()
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
					results[i] = targets[i].getMountPath() + " - " + targets[i].toString();
				}
				return results;
			}
		}
		return null;
	}

	/**
	 * @see wicket.jmx.ApplicationMBean#getWicketVersion()
	 */
	public String getWicketVersion() throws IOException
	{
		return application.getFrameworkSettings().getVersion();
	}
}
