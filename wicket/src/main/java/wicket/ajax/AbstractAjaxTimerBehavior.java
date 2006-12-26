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
package wicket.ajax;

import wicket.markup.html.IHeaderResponse;
import wicket.markup.html.WebPage;
import wicket.util.time.Duration;

/**
 * A behavior that generates an AJAX update callback at a regular interval.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AbstractAjaxTimerBehavior extends AbstractDefaultAjaxBehavior
{
	/** The update interval */
	private final Duration updateInterval;

	private boolean attachedBodyOnLoadModifier = false;

	private boolean stopped = false;

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
	 * Stops the timer
	 */
	public final void stop()
	{
		stopped = true;
	}

	/**
	 * @see wicket.behavior.AbstractAjaxBehavior#renderHead(wicket.markup.html.IHeaderResponse)
	 */
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		if (this.attachedBodyOnLoadModifier == false)
		{
			this.attachedBodyOnLoadModifier = true;
			((WebPage)getComponent().getPage()).getBodyContainer().addOnLoadModifier(
					getJsTimeoutCall(updateInterval), getComponent());
		}
	}

	/**
	 * @param updateInterval
	 *            Duration between AJAX callbacks
	 * @return JS script
	 */
	protected final String getJsTimeoutCall(final Duration updateInterval)
	{
		return "setTimeout(function() { " + getCallbackScript(false, true) + " }, "
				+ updateInterval.getMilliseconds() + ");";
	}

	/**
	 * 
	 * @see wicket.ajax.AbstractDefaultAjaxBehavior#respond(wicket.ajax.AjaxRequestTarget)
	 */
	protected final void respond(final AjaxRequestTarget target)
	{
		onTimer(target);

		if (!stopped)
		{
			// this might look strange, but it is necessary for IE not to leak
			String js = "setTimeout(\"" + getCallbackScript(false, true) + "\", "
					+ updateInterval.getMilliseconds() + ");";

			target.appendJavascript(js);
		}
	}

	/**
	 * Listener method for the AJAX timer event.
	 * 
	 * @param target
	 *            The request target
	 */
	protected abstract void onTimer(final AjaxRequestTarget target);
}
