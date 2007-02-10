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
public class DebugSettings implements DebugSettingsMBean
{
	private final wicket.Application application;

	/**
	 * Create.
	 * 
	 * @param application
	 */
	public DebugSettings(wicket.Application application)
	{
		this.application = application;
	}

	/**
	 * @see wicket.jmx.DebugSettingsMBean#getComponentUseCheck()
	 */
	public boolean getComponentUseCheck()
	{
		return application.getDebugSettings().getComponentUseCheck();
	}

	/**
	 * @see wicket.jmx.DebugSettingsMBean#getSerializeSessionAttributes()
	 */
	public boolean getSerializeSessionAttributes()
	{
		return application.getDebugSettings().getSerializeSessionAttributes();
	}

	/**
	 * @see wicket.jmx.DebugSettingsMBean#isAjaxDebugModeEnabled()
	 */
	public boolean isAjaxDebugModeEnabled()
	{
		return application.getDebugSettings().isAjaxDebugModeEnabled();
	}

	/**
	 * @see wicket.jmx.DebugSettingsMBean#setAjaxDebugModeEnabled(boolean)
	 */
	public void setAjaxDebugModeEnabled(boolean enable)
	{
		application.getDebugSettings().setAjaxDebugModeEnabled(enable);
	}

	/**
	 * @see wicket.jmx.DebugSettingsMBean#setComponentUseCheck(boolean)
	 */
	public void setComponentUseCheck(boolean check)
	{
		application.getDebugSettings().setComponentUseCheck(check);
	}

	/**
	 * @see wicket.jmx.DebugSettingsMBean#setSerializeSessionAttributes(boolean)
	 */
	public void setSerializeSessionAttributes(boolean serialize)
	{
		application.getDebugSettings().setSerializeSessionAttributes(serialize);
	}
}
