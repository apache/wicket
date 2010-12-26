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
package org.apache.wicket.settings.def;

import org.apache.wicket.settings.IDebugSettings;

/**
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class DebugSettings implements IDebugSettings
{
	/** ajax debug mode status */
	private boolean ajaxDebugModeEnabled = false;

	/** True to check that each component on a page is used */
	private boolean componentUseCheck = true;

	/**
	 * whether wicket should track line precise additions of components for error reporting.
	 */
	private boolean linePreciseReportingOnAddComponentEnabled = false;

	/**
	 * whether wicket should track line precise instantiations of components for error reporting.
	 */
	private boolean linePreciseReportingOnNewComponentEnabled = false;

	/**
	 * Whether the container's class name should be printed to response (in a html comment).
	 */
	private boolean outputMarkupContainerClassName = false;

	/** @see IDebugSettings#setOutputComponentPath(boolean) */
	private boolean outputComponentPath = false;

	/** @see IDebugSettings#setDevelopmentUtilitiesEnabled(boolean) */
	private boolean developmentUtilitiesEnabled = false;

	/**
	 * @see org.apache.wicket.settings.IDebugSettings#getComponentUseCheck()
	 */
	public boolean getComponentUseCheck()
	{
		return componentUseCheck;
	}

	/**
	 * @see org.apache.wicket.settings.IDebugSettings#isAjaxDebugModeEnabled()
	 */
	public boolean isAjaxDebugModeEnabled()
	{
		return ajaxDebugModeEnabled;
	}

	/**
	 * 
	 * @see org.apache.wicket.settings.IDebugSettings#isLinePreciseReportingOnAddComponentEnabled()
	 */
	public boolean isLinePreciseReportingOnAddComponentEnabled()
	{
		return linePreciseReportingOnAddComponentEnabled;
	}

	/**
	 * 
	 * @see org.apache.wicket.settings.IDebugSettings#isLinePreciseReportingOnNewComponentEnabled()
	 */
	public boolean isLinePreciseReportingOnNewComponentEnabled()
	{
		return linePreciseReportingOnNewComponentEnabled;
	}

	/**
	 * @see org.apache.wicket.settings.IDebugSettings#isOutputMarkupContainerClassName()
	 */
	public boolean isOutputMarkupContainerClassName()
	{
		return outputMarkupContainerClassName;
	}

	/**
	 * @see org.apache.wicket.settings.IDebugSettings#setAjaxDebugModeEnabled(boolean)
	 */
	public void setAjaxDebugModeEnabled(boolean enable)
	{
		ajaxDebugModeEnabled = enable;
	}

	/**
	 * @see org.apache.wicket.settings.IDebugSettings#setComponentUseCheck(boolean)
	 */
	public void setComponentUseCheck(final boolean componentUseCheck)
	{
		this.componentUseCheck = componentUseCheck;
	}

	public void setLinePreciseReportingOnAddComponentEnabled(boolean enable)
	{
		linePreciseReportingOnAddComponentEnabled = enable;
	}

	/**
	 * 
	 * @see org.apache.wicket.settings.IDebugSettings#setLinePreciseReportingOnNewComponentEnabled(boolean)
	 */
	public void setLinePreciseReportingOnNewComponentEnabled(boolean enable)
	{
		linePreciseReportingOnNewComponentEnabled = enable;
	}

	/**
	 * @see org.apache.wicket.settings.IDebugSettings#setOutputMarkupContainerClassName(boolean)
	 */
	public void setOutputMarkupContainerClassName(boolean enable)
	{
		outputMarkupContainerClassName = enable;
	}

	/** @see IDebugSettings#isOutputComponentPath() */
	public boolean isOutputComponentPath()
	{
		return outputComponentPath;
	}

	/** @see IDebugSettings#setOutputComponentPath(boolean) */
	public void setOutputComponentPath(boolean outputComponentPath)
	{
		this.outputComponentPath = outputComponentPath;
	}

	/**
	 * @see org.apache.wicket.settings.IDebugSettings#setDevelopmentUtilitiesEnabled(boolean)
	 */
	public void setDevelopmentUtilitiesEnabled(boolean enable)
	{
		developmentUtilitiesEnabled = enable;
	}

	/**
	 * @see org.apache.wicket.settings.IDebugSettings#isDevelopmentUtilitiesEnabled()
	 */
	public boolean isDevelopmentUtilitiesEnabled()
	{
		return developmentUtilitiesEnabled;
	}
}
