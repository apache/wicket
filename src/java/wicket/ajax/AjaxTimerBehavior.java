/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
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
package wicket.ajax;

import wicket.Response;

/**
 * 
 */
public abstract class AjaxTimerBehavior extends AjaxBehavior
{
	private long millis;

	/**
	 * Construct.
	 * 
	 * @param millis
	 */
	public AjaxTimerBehavior(long millis)
	{
		this.millis = millis;
	}

	protected void onRenderHeadInitContribution(Response response)
	{
		super.onRenderHeadInitContribution(response);
		getBodyContainer(null).addOnLoadModifier(getJsTimeoutCall(millis));
	}

	/**
	 * @param millis
	 * 
	 * @return JS script 
	 */
	protected final String getJsTimeoutCall(final long millis)
	{
		return "setTimeout(function() { " + buildAjaxCall() + " }, " + millis + ");";
	}

	/**
	 * 
	 * @see wicket.ajax.AjaxBehavior#respond(wicket.ajax.AjaxRequestTarget)
	 */
	protected void respond(final AjaxRequestTarget target)
	{
		onTimer(target);
		target.addJavascript(getJsTimeoutCall(millis));
	}

	/**
	 * 
	 * @param target
	 */
	protected abstract void onTimer(final AjaxRequestTarget target);
}
