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


/**
 * Exposes Application related functionality for JMX.
 * 
 * @author eelcohillenius
 */
public class PageSettings implements PageSettingsMBean
{
	private final wicket.Application application;

	/**
	 * Create.
	 * 
	 * @param application
	 */
	public PageSettings(wicket.Application application)
	{
		this.application = application;
	}

	/**
	 * @see wicket.jmx.PageSettingsMBean#getAutomaticMultiWindowSupport()
	 */
	public boolean getAutomaticMultiWindowSupport()
	{
		return application.getPageSettings().getAutomaticMultiWindowSupport();
	}

	/**
	 * @see wicket.jmx.PageSettingsMBean#getMaxPageVersions()
	 */
	public int getMaxPageVersions()
	{
		return application.getPageSettings().getMaxPageVersions();
	}

	/**
	 * @see wicket.jmx.PageSettingsMBean#getVersionPagesByDefault()
	 */
	public boolean getVersionPagesByDefault()
	{
		return application.getPageSettings().getVersionPagesByDefault();
	}

	/**
	 * @see wicket.jmx.PageSettingsMBean#setAutomaticMultiWindowSupport(boolean)
	 */
	public void setAutomaticMultiWindowSupport(boolean automaticMultiWindowSupport)
	{
		application.getPageSettings().setAutomaticMultiWindowSupport(automaticMultiWindowSupport);
	}

	/**
	 * @see wicket.jmx.PageSettingsMBean#setMaxPageVersions(int)
	 */
	public void setMaxPageVersions(int maxPageVersions)
	{
		application.getPageSettings().setMaxPageVersions(maxPageVersions);
	}

	/**
	 * @see wicket.jmx.PageSettingsMBean#setVersionPagesByDefault(boolean)
	 */
	public void setVersionPagesByDefault(boolean pagesVersionedByDefault)
	{
		application.getPageSettings().setVersionPagesByDefault(pagesVersionedByDefault);
	}
}
