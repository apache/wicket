/*
 * $Id$
 * $Revision$ $Date$
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
import wicket.util.time.Duration;

/**
 * A behavior that generates an ajax callback every x number of milliseconds
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

	/**
	 * Constructor that works with the convinience {@link Duration} class that
	 * leads to cleaner, more readible code.
	 * 
	 * @param duration
	 */
	public AjaxTimerBehavior(Duration duration)
	{
		this(duration.getMilliseconds());
	}

	protected void onRenderHeadContribution(Response response)
	{
		super.onRenderHeadContribution(response);
		getBodyContainer().addOnLoadModifier(getJsTimeoutCall(millis));
	}

	/**
	 * @param millis
	 * 
	 * @return JS script
	 */
	protected final String getJsTimeoutCall(final long millis)
	{
		return "setTimeout(function() { wicketAjaxGet('" + getCallbackUrl() + "'); }, " + millis
				+ ");";
	}

	/**
	 * 
	 * @see wicket.ajax.AjaxBehavior#respond(wicket.ajax.AjaxRequestTarget)
	 */
	protected final void respond(final AjaxRequestTarget target)
	{
		onTimer(target);
		target.addJavascript(getJsTimeoutCall(millis));
	}

	/**
	 * Listener method for the ajax timer event
	 * 
	 * @param target
	 */
	protected abstract void onTimer(final AjaxRequestTarget target);
}
