/*
 * $Id: org.eclipse.jdt.ui.prefs,v 1.6 2006/02/06 08:27:03 ivaynberg Exp $
 * $Revision: 1.6 $ $Date: 2006/02/06 08:27:03 $
 * 
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
package wicket.ajax.calldecorator;

import wicket.ajax.IAjaxCallDecorator;

/**
 * Simple implementation of IAjaxCallDecorator
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class AjaxCallDecorator implements IAjaxCallDecorator
{
	private String beforeScript;
	private String afterScript;
	private String onSuccessScript;
	private String onFailureScript;

	/**
	 * Construct.
	 */
	public AjaxCallDecorator()
	{

	}

	/**
	 * @see wicket.ajax.IAjaxCallDecorator#getBeforeScript()
	 */
	public String getBeforeScript()
	{
		return beforeScript;
	}

	/**
	 * @see wicket.ajax.IAjaxCallDecorator#getAfterScript()
	 */
	public String getAfterScript()
	{
		return afterScript;
	}

	/**
	 * @see wicket.ajax.IAjaxCallDecorator#getOnSuccessScript()
	 */
	public String getOnSuccessScript()
	{
		return onSuccessScript;
	}

	/**
	 * @see wicket.ajax.IAjaxCallDecorator#getOnFailureScript()
	 */
	public String getOnFailureScript()
	{
		return onFailureScript;
	}

	/**
	 * Setter for after script
	 * 
	 * @param afterScript
	 * @return this for chaining
	 * @see IAjaxCallDecorator#getAfterScript()
	 */
	public AjaxCallDecorator setAfterScript(String afterScript)
	{
		this.afterScript = afterScript;
		return this;
	}

	/**
	 * Setter for before script
	 * 
	 * @param beforeScript
	 * @return this for chaining
	 * @see IAjaxCallDecorator#getBeforeScript()
	 */
	public AjaxCallDecorator setBeforeScript(String beforeScript)
	{
		this.beforeScript = beforeScript;
		return this;
	}

	/**
	 * Setter for failure script
	 * 
	 * @param failureHandler
	 * @return this for chaining
	 * @see IAjaxCallDecorator#getOnFailureScript()
	 */
	public AjaxCallDecorator setOnFailureScript(String failureHandler)
	{
		this.onFailureScript = failureHandler;
		return this;
	}

	/**
	 * Setter for success script
	 * 
	 * @param successHandler
	 * @return this for chaining
	 * @see IAjaxCallDecorator#getOnSuccessScript()
	 */
	public AjaxCallDecorator setOnSuccessScript(String successHandler)
	{
		this.onSuccessScript = successHandler;
		return this;
	}


}
