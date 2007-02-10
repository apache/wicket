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
public class SessionSettings implements SessionSettingsMBean
{
	private final wicket.Application application;

	/**
	 * Create.
	 * 
	 * @param application
	 */
	public SessionSettings(wicket.Application application)
	{
		this.application = application;
	}

	/**
	 * @see wicket.jmx.SessionSettingsMBean#getMaxPageMaps()
	 */
	public int getMaxPageMaps()
	{
		return application.getSessionSettings().getMaxPageMaps();
	}

	/**
	 * @see wicket.jmx.SessionSettingsMBean#getPageFactory()
	 */
	public String getPageFactory()
	{
		return Stringz.className(application.getSessionSettings().getPageFactory());
	}

	/**
	 * @see wicket.jmx.SessionSettingsMBean#getPageMapEvictionStrategy()
	 */
	public String getPageMapEvictionStrategy()
	{
		return Stringz.className(application.getSessionSettings().getPageMapEvictionStrategy());
	}

	/**
	 * @see wicket.jmx.SessionSettingsMBean#getSessionStore()
	 */
	public String getSessionStore()
	{
		return Stringz.className(application.getSessionStore());
	}

	/**
	 * @see wicket.jmx.SessionSettingsMBean#setMaxPageMaps(int)
	 */
	public void setMaxPageMaps(int maxPageMaps)
	{
		application.getSessionSettings().setMaxPageMaps(maxPageMaps);
	}

}
