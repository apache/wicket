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
package org.apache.wicket.settings;

/**
 * Settings interface for various debug settings
 * <p>
 * <i>componentUseCheck </i> (defaults to true in development mode) - causes the framework to do a
 * check after rendering each page to ensure that each component was used in rendering the markup.
 * If components are found that are not referenced in the markup, an appropriate error will be
 * displayed
 *
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class DebugSettings
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

	private boolean outputComponentPath = false;

	private String componentPathAttributeName = null;

	private boolean developmentUtilitiesEnabled = false;

	/**
	 * @return true if componentUseCheck is enabled
	 */
	public boolean getComponentUseCheck()
	{
		return componentUseCheck;
	}

	/**
	 * Returns status of ajax debug mode.
	 *
	 * @return true if ajax debug mode is enabled, false otherwise
	 */
	public boolean isAjaxDebugModeEnabled()
	{
		return ajaxDebugModeEnabled;
	}

	/**
	 * Returns status of line precise error reporting for added components that are not present in
	 * the markup: it points to the line where the component was added to the hierarchy in your Java
	 * classes. This can cause a significant decrease in performance, do not use in customer facing
	 * applications.
	 *
	 * @return true if the line precise error reporting is enabled
	 */
	public boolean isLinePreciseReportingOnAddComponentEnabled()
	{
		return linePreciseReportingOnAddComponentEnabled;
	}

	/**
	 * Returns status of line precise error reporting for new components that are not present in the
	 * markup: it points to the line where the component was created in your Java classes. This can
	 * cause a significant decrease in performance, do not use in customer facing applications.
	 *
	 * @return true if the line precise error reporting is enabled
	 */
	public boolean isLinePreciseReportingOnNewComponentEnabled()
	{
		return linePreciseReportingOnNewComponentEnabled;
	}

	/**
	 * Returns whether the output of markup container's should be wrapped by comments containing the
	 * container's class name.
	 *
	 * @return true if the markup container's class name should be written to response
	 */
	public boolean isOutputMarkupContainerClassName()
	{
		return outputMarkupContainerClassName;
	}

	/**
	 * Enables or disables ajax debug mode.
	 *
	 * @param enable
	 * @return {@code this} object for chaining
	 */
	public DebugSettings setAjaxDebugModeEnabled(boolean enable)
	{
		ajaxDebugModeEnabled = enable;
		return this;
	}

	/**
	 * Sets componentUseCheck debug settings
	 *
	 * @param componentUseCheck
	 * @return {@code this} object for chaining
	 */
	public DebugSettings setComponentUseCheck(final boolean componentUseCheck)
	{
		this.componentUseCheck = componentUseCheck;
		return this;
	}

	/**
	 * Enables line precise error reporting for added components that are not present in the markup:
	 * it points to the line where the component was added to the hierarchy in your Java classes.
	 * This can cause a significant decrease in performance, do not use in customer facing
	 * applications.
	 *
	 * @param enable
	 * @return {@code this} object for chaining
	 */
	public DebugSettings setLinePreciseReportingOnAddComponentEnabled(boolean enable)
	{
		linePreciseReportingOnAddComponentEnabled = enable;
		return this;
	}

	/**
	 * Enables line precise error reporting for new components that are not present in the markup:
	 * it points to the line where the component was created in your Java classes. This can cause a
	 * significant decrease in performance, do not use in customer facing applications.
	 *
	 * @param enable
	 * @return {@code this} object for chaining
	 */
	public DebugSettings setLinePreciseReportingOnNewComponentEnabled(boolean enable)
	{
		linePreciseReportingOnNewComponentEnabled = enable;
		return this;
	}

	/**
	 * Enables wrapping output of markup container in html comments that contain markup container's
	 * class name. (Useful for determining which part of page belongs to which markup file).
	 *
	 * @param enable
	 * @return {@code this} object for chaining
	 */
	public DebugSettings setOutputMarkupContainerClassName(boolean enable)
	{
		outputMarkupContainerClassName = enable;
		return this;
	}

	/**
	 * @see #setOutputComponentPath(boolean)
	 * @return <code>true</code> if output component path feature is enabled, <code>false</code>
	 *         otherwise
	 * @deprecated Use #getComponentPathAttributeName() instead
	 */
	@Deprecated
	public boolean isOutputComponentPath()
	{
		return outputComponentPath;
	}

	/**
	 * If set to <code>true</code> wicket will output component path in a <code>wicketpath</code>
	 * attribute of the component tag. This can be useful for debugging and automating tests.
	 *
	 * @param outputComponentPath
	 * @return {@code this} object for chaining
	 * @deprecated Use #setComponentPathAttributeName() with a non-empty value
	 */
	@Deprecated
	public DebugSettings setOutputComponentPath(boolean outputComponentPath)
	{
		this.outputComponentPath = outputComponentPath;
		return this;
	}

	/**
	 * If the parameter value is non-empty then Wicket will use it as the name of an attribute of the
	 * component tag to print the {@link org.apache.wicket.Component}'s path.
	 * This can be useful for debugging and automating tests.
	 *
	 * For example: if {@code componentPathAttributeName} is 'data-wicket-path' then Wicket will add
	 * an attribute to the {@link org.apache.wicket.markup.ComponentTag} for each component with name
	 * 'data-wicket-path' and as a value the component's
	 * {@link org.apache.wicket.Component#getPageRelativePath() page relative path}.
	 *
	 * @param componentPathAttributeName
	 *          The name of the attribute for the {@link org.apache.wicket.markup.ComponentTag}.
	 *          If {@code null} or empty then the attribute won't be rendered
	 * @return {@code this} object for chaining
	 */
	public DebugSettings setComponentPathAttributeName(String componentPathAttributeName)
	{
		this.componentPathAttributeName = componentPathAttributeName;
		return this;
	}

	/**
	 * @see #setComponentPathAttributeName(String)
	 * @return The name of the attribute for the {@link org.apache.wicket.markup.ComponentTag}.
	 *         If {@code null} or empty then the attribute won't be rendered
	 */
	public String getComponentPathAttributeName()
	{
		return componentPathAttributeName;
	}


	/**
	 * Enables all of the panels and pages, etc, from wicket-devutils package.
	 *
	 * @param enable
	 * @return {@code this} object for chaining
	 */
	public DebugSettings setDevelopmentUtilitiesEnabled(boolean enable)
	{
		developmentUtilitiesEnabled = enable;
		return this;
	}

	/**
	 * Are all of the panels and pages, etc, from wicket-devutils package enabled?
	 *
	 * @return true if all of the panels and pages, etc, from wicket-devutils package are enabled
	 */
	public boolean isDevelopmentUtilitiesEnabled()
	{
		return developmentUtilitiesEnabled;
	}
}
