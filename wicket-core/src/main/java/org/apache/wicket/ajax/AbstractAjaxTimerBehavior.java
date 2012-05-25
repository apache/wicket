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
package org.apache.wicket.ajax;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.string.JavaScriptUtils;
import org.apache.wicket.util.time.Duration;

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
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The update interval */
	private Duration updateInterval;

	private boolean stopped = false;

	private boolean headRendered = false;

	/**
	 * Construct.
	 * 
	 * @param updateInterval
	 *            Duration between AJAX callbacks
	 */
	public AbstractAjaxTimerBehavior(final Duration updateInterval)
	{
		if (updateInterval == null || updateInterval.getMilliseconds() <= 0)
		{
			throw new IllegalArgumentException("Invalid update interval");
		}
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
	 * Sets the update interval duration. This method should only be called within the
	 * {@link #onTimer(AjaxRequestTarget)} method.
	 * 
	 * @param updateInterval
	 */
	protected final void setUpdateInterval(Duration updateInterval)
	{
		if (updateInterval == null || updateInterval.getMilliseconds() <= 0)
		{
			throw new IllegalArgumentException("Invalid update interval");
		}
		this.updateInterval = updateInterval;
	}

	/**
	 * Returns the update interval
	 * 
	 * @return The update interval
	 */
	public final Duration getUpdateInterval()
	{
		return updateInterval;
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		WebRequest request = (WebRequest)RequestCycle.get().getRequest();

		if (!stopped && (!headRendered || !request.isAjax()))
		{
			headRendered = true;
			response.renderOnLoadJavaScript(getJsTimeoutCall(updateInterval));
		}
	}

	/**
	 * @param updateInterval
	 *            Duration between AJAX callbacks
	 * @return JS script
	 */
	protected final String getJsTimeoutCall(final Duration updateInterval)
	{
		CharSequence callbackScript = getCallbackScript();
		callbackScript = JavaScriptUtils.escapeQuotes(callbackScript);
		// use setTimeout(String) instead of setTimeout(function) because IE leaks memory
		return "setTimeout(\"" + callbackScript + "\", " + updateInterval.getMilliseconds() +
			");";
	}

	@Override
	protected CharSequence getCallbackScript()
	{
		return generateCallbackScript("wicketAjaxGet('" + getCallbackUrl() + "'");
	}

	/**
	 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getPreconditionScript()
	 */
	@Override
	protected CharSequence getPreconditionScript()
	{
		String precondition = null;
		if (!(getComponent() instanceof Page))
		{
			String componentId = getComponent().getMarkupId();
			precondition = "var c = Wicket.$('" + componentId +
				"'); return typeof(c) != 'undefined' && c != null";
		}
		return precondition;
	}

	/**
	 * 
	 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#respond(org.apache.wicket.ajax.AjaxRequestTarget)
	 */
	@Override
	protected final void respond(final AjaxRequestTarget target)
	{
		onTimer(target);

		if (!stopped && isEnabled(getComponent()))
		{
			target.getHeaderResponse().renderOnLoadJavaScript(getJsTimeoutCall(updateInterval));
		}
	}

	/**
	 * Listener method for the AJAX timer event.
	 * 
	 * @param target
	 *            The request target
	 */
	protected abstract void onTimer(final AjaxRequestTarget target);

	/**
	 * @return {@code true} if has been stopped via {@link #stop()}
	 */
	public final boolean isStopped()
	{
		return stopped;
	}


}
