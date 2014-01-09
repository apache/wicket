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
 * Debug settings.
 * 
 * @author eelcohillenius
 */
public interface DebugSettingsMBean
{
	/**
	 * @return true if componentUseCheck is enabled
	 */
	boolean getComponentUseCheck();

	/**
	 * Returns status of ajax debug mode.
	 * 
	 * @return true if ajax debug mode is enabled, false otherwise
	 * 
	 */
	boolean isAjaxDebugModeEnabled();

	/**
	 * Enables or disables ajax debug mode.
	 * 
	 * @param enable
	 * 
	 */
	void setAjaxDebugModeEnabled(boolean enable);

	/**
	 * Sets componentUseCheck debug settings
	 * 
	 * @param check
	 */
	void setComponentUseCheck(boolean check);


	/**
	 * If set to <code>true</code> wicket will output component path in a <code>wicket:path</code>
	 * attribute of the component tag. This can be useful for debugging and automating tests.
	 * 
	 * @param enabled
	 */
	public void setOutputComponentPath(boolean enabled);

	/**
	 * @see #setOutputComponentPath(boolean)
	 * @return <code>true</code> if output component path feature is enabled, <code>false</code>
	 *         otherwise
	 */
	public boolean isOutputComponentPath();

	/**
	 * Enables wrapping output of markup container in html comments that contain markup container's
	 * class name. (Useful for determining which part of page belongs to which markup file).
	 * 
	 * @param enable
	 */
	public void setOutputMarkupContainerClassName(boolean enable);

	/**
	 * Returns whether the output of markup container's should be wrapped by comments containing the
	 * container's class name.
	 * 
	 * @return true if the markup container's class name should be written to response
	 */
	public boolean isOutputMarkupContainerClassName();

	/**
	 * Returns status of line precise error reporting for added components that are not present in
	 * the markup: it points to the line where the component was added to the hierarchy in your Java
	 * classes. This can cause a significant decrease in performance, do not use in customer facing
	 * applications.
	 * 
	 * @return true if the line precise error reporting is enabled
	 */
	public boolean isLinePreciseReportingOnAddComponentEnabled();

	/**
	 * Enables line precise error reporting for added components that are not present in the markup:
	 * it points to the line where the component was added to the hierarchy in your Java classes.
	 * This can cause a significant decrease in performance, do not use in customer facing
	 * applications.
	 * 
	 * @param enable
	 */
	public void setLinePreciseReportingOnAddComponentEnabled(boolean enable);

	/**
	 * Returns status of line precise error reporting for new components that are not present in the
	 * markup: it points to the line where the component was created in your Java classes. This can
	 * cause a significant decrease in performance, do not use in customer facing applications.
	 * 
	 * @return true if the line precise error reporting is enabled
	 */
	public boolean isLinePreciseReportingOnNewComponentEnabled();

	/**
	 * Enables line precise error reporting for new components that are not present in the markup:
	 * it points to the line where the component was created in your Java classes. This can cause a
	 * significant decrease in performance, do not use in customer facing applications.
	 * 
	 * @param enable
	 */
	public void setLinePreciseReportingOnNewComponentEnabled(boolean enable);

	/**
	 * Enables all of the panels and pages, etc, from wicket-devutils package.
	 * 
	 * @param enable
	 */
	public void setDevelopmentUtilitiesEnabled(boolean enable);

	/**
	 * Are all of the panels and pages, etc, from wicket-devutils package enabled?
	 * 
	 * @return true if all of the panels and pages, etc, from wicket-devutils package are enabled
	 */
	public boolean isDevelopmentUtilitiesEnabled();
}
