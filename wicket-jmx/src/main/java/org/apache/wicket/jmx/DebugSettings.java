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

/**
 * Exposes Application related functionality for JMX.
 * 
 * @author eelcohillenius
 */
public class DebugSettings implements DebugSettingsMBean
{
	private final org.apache.wicket.Application application;

	/**
	 * Create.
	 * 
	 * @param application
	 */
	public DebugSettings(final org.apache.wicket.Application application)
	{
		this.application = application;
	}

	/**
	 * @see org.apache.wicket.jmx.DebugSettingsMBean#getComponentUseCheck()
	 */
	@Override
	public boolean getComponentUseCheck()
	{
		return application.getDebugSettings().getComponentUseCheck();
	}

	/**
	 * @see org.apache.wicket.jmx.DebugSettingsMBean#isAjaxDebugModeEnabled()
	 */
	@Override
	public boolean isAjaxDebugModeEnabled()
	{
		return application.getDebugSettings().isAjaxDebugModeEnabled();
	}

	/**
	 * @see org.apache.wicket.jmx.DebugSettingsMBean#setAjaxDebugModeEnabled(boolean)
	 */
	@Override
	public void setAjaxDebugModeEnabled(final boolean enable)
	{
		application.getDebugSettings().setAjaxDebugModeEnabled(enable);
	}

	/**
	 * @see org.apache.wicket.jmx.DebugSettingsMBean#setComponentUseCheck(boolean)
	 */
	@Override
	public void setComponentUseCheck(final boolean check)
	{
		application.getDebugSettings().setComponentUseCheck(check);
	}

	/**
	 * @see org.apache.wicket.jmx.DebugSettingsMBean#setOutputComponentPath(boolean)
	 */
	@Override
	public void setOutputComponentPath(final boolean enabled)
	{
		application.getDebugSettings().setOutputComponentPath(enabled);
	}

	/**
	 * @see org.apache.wicket.jmx.DebugSettingsMBean#isOutputComponentPath()
	 */
	@Override
	public boolean isOutputComponentPath()
	{
		return application.getDebugSettings().isOutputComponentPath();
	}


	/**
	 * @see org.apache.wicket.jmx.DebugSettingsMBean#setOutputMarkupContainerClassName(boolean)
	 */
	@Override
	public void setOutputMarkupContainerClassName(final boolean enable)
	{
		application.getDebugSettings().setOutputMarkupContainerClassName(enable);
	}

	/**
	 * @see org.apache.wicket.jmx.DebugSettingsMBean#isOutputMarkupContainerClassName()
	 */
	@Override
	public boolean isOutputMarkupContainerClassName()
	{
		return application.getDebugSettings().isOutputMarkupContainerClassName();
	}

	/**
	 * @see org.apache.wicket.jmx.DebugSettingsMBean#isLinePreciseReportingOnAddComponentEnabled()
	 */
	@Override
	public boolean isLinePreciseReportingOnAddComponentEnabled()
	{
		return application.getDebugSettings().isLinePreciseReportingOnAddComponentEnabled();
	}

	/**
	 * @see org.apache.wicket.jmx.DebugSettingsMBean#setLinePreciseReportingOnAddComponentEnabled(boolean)
	 */
	@Override
	public void setLinePreciseReportingOnAddComponentEnabled(final boolean enable)
	{
		application.getDebugSettings().setLinePreciseReportingOnAddComponentEnabled(enable);
	}

	/**
	 * @see org.apache.wicket.jmx.DebugSettingsMBean#isLinePreciseReportingOnNewComponentEnabled()
	 */
	@Override
	public boolean isLinePreciseReportingOnNewComponentEnabled()
	{
		return application.getDebugSettings().isLinePreciseReportingOnNewComponentEnabled();
	}

	/**
	 * @see org.apache.wicket.jmx.DebugSettingsMBean#setLinePreciseReportingOnNewComponentEnabled(boolean)
	 */
	@Override
	public void setLinePreciseReportingOnNewComponentEnabled(final boolean enable)
	{
		application.getDebugSettings().setLinePreciseReportingOnNewComponentEnabled(enable);
	}

	/**
	 * @see org.apache.wicket.jmx.DebugSettingsMBean#setDevelopmentUtilitiesEnabled(boolean)
	 */
	@Override
	public void setDevelopmentUtilitiesEnabled(final boolean enable)
	{
		application.getDebugSettings().setDevelopmentUtilitiesEnabled(enable);
	}

	/**
	 * @see org.apache.wicket.jmx.DebugSettingsMBean#isDevelopmentUtilitiesEnabled()
	 */
	@Override
	public boolean isDevelopmentUtilitiesEnabled()
	{
		return application.getDebugSettings().isDevelopmentUtilitiesEnabled();
	}
}
