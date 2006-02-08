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
 * A behavior that generates an AJAX update callback at a regular interval.
 */
public abstract class AbstractAjaxTimerBehavior extends AjaxBehavior
{
	/** The update interval */
	private final Duration updateInterval;

	/**
	 * Construct.
	 * 
	 * @param updateInterval
	 *            Duration between AJAX callbacks
	 */
	public AbstractAjaxTimerBehavior(final Duration updateInterval)
	{
		this.updateInterval = updateInterval;
	}

	/**
	 * @see wicket.behavior.AbstractAjaxBehavior#onRenderHeadContribution(wicket.Response)
	 */
	protected void onRenderHeadContribution(final Response response)
	{
		super.onRenderHeadContribution(response);
		getBodyContainer().addOnLoadModifier(getJsTimeoutCall(updateInterval));
	}

	/**
	 * @param updateInterval
	 *            Duration between AJAX callbacks
	 * @return JS script
	 */
	protected final String getJsTimeoutCall(final Duration updateInterval)
	{
		return "setTimeout(function() { wicketAjaxGet('" + getCallbackUrl() + "'); }, "
				+ updateInterval.getMilliseconds() + ");";
	}

	/**
	 * 
	 * @see wicket.ajax.AjaxBehavior#respond(wicket.ajax.AjaxRequestTarget)
	 */
	protected final void respond(final AjaxRequestTarget target)
	{
		onTimer(target);
		target.addJavascript(getJsTimeoutCall(updateInterval));
	}

	/**
	 * Listener method for the AJAX timer event.
	 * 
	 * @param target
	 *            The request target
	 */
	protected abstract void onTimer(final AjaxRequestTarget target);
}
