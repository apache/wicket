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
package wicket.settings;

/**
 * Settings interface for various debug settings
 * <p>
 * <i>componentUseCheck </i> (defaults to true) - Causes the framework to do a
 * check after rendering each page to ensure that each component was used in
 * rendering the markup. If components are found that are not referenced in the
 * markup, an appropriate error will be displayed <i>serializeSessionAttributes</i>
 * (defaults to true in devel mode) - Causes the framework to serialize any
 * attribute put into session - this helps find Not Serializable errors early
 * <p>
 * 
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IDebugSettings
{
	/**
	 * @return true if componentUseCheck is enabled
	 */
	boolean getComponentUseCheck();

	/**
	 * Sets componentUseCheck debug settings
	 * 
	 * @param check
	 */
	void setComponentUseCheck(boolean check);

	/**
	 * Enables or disables ajax debug mode. See {@link IAjaxSettings} for
	 * details
	 * 
	 * @param enable
	 * 
	 */
	void setAjaxDebugModeEnabled(boolean enable);

	/**
	 * Returns status of ajax debug mode. See {@link IAjaxSettings} for details
	 * 
	 * @return true if ajax debug mode is enabled, false otherwise
	 * 
	 */
	boolean isAjaxDebugModeEnabled();

	/**
	 * Sets the seriaalize session attributes setting
	 * 
	 * @param serialize
	 */
	void setSerializeSessionAttributes(boolean serialize);

	/**
	 * @return true if serialize session attributes is enabled, false otherwise
	 */
	boolean getSerializeSessionAttributes();

}
