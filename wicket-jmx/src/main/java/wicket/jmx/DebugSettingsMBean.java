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
package wicket.jmx;

import wicket.settings.IDebugSettings;

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
	 * @return true if serialize session attributes is enabled, false otherwise
	 */
	boolean getSerializeSessionAttributes();

	/**
	 * Returns status of ajax debug mode. See {@link IDebugSettings} for details
	 * 
	 * @return true if ajax debug mode is enabled, false otherwise
	 * 
	 */
	boolean isAjaxDebugModeEnabled();

	/**
	 * Enables or disables ajax debug mode. See {@link IDebugSettings} for
	 * details
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
	 * Sets the seriaalize session attributes setting
	 * 
	 * @param serialize
	 */
	void setSerializeSessionAttributes(boolean serialize);
}
