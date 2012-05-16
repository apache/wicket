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
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.request.http.WebRequest;
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
		setUpdateInterval(updateInterval);
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

		WebRequest request = (WebRequest) component.getRequest();

		if (!isStopped() && (!headRendered || !request.isAjax()))
		{
			headRendered = true;
			response.render(OnLoadHeaderItem.forScript(getJsTimeoutCall(updateInterval)));
		}
	}

	/**
	 * @param updateInterval
	 *            Duration between AJAX callbacks
	 * @return JS script
	 */
	protected final String getJsTimeoutCall(final Duration updateInterval)
	{
		CharSequence js = getCallbackScript();
		js = JavaScriptUtils.escapeQuotes(js);

		String timeoutHandle = getTimeoutHandle();
		// this might look strange, but it is necessary for IE not to leak :(
		return timeoutHandle+" = setTimeout('" + js + "', " +
			updateInterval.getMilliseconds() + ")";
	}

	/**
	 * @return the name of the handle that is used to stop any scheduled timer
	 */
	private String getTimeoutHandle() {
		return "Wicket.timerHandle_"+getComponent().getMarkupId();
	}
	
	/**
	 * 
	 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#respond(AjaxRequestTarget)
	 */
	@Override
	protected final void respond(final AjaxRequestTarget target)
	{
		if (!isStopped() && isEnabled(getComponent()))
		{
			onTimer(target);

			target.getHeaderResponse().render(
			OnLoadHeaderItem.forScript(getJsTimeoutCall(updateInterval)));
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
	 * @return {@code true} if has been stopped via {@link #stop(AjaxRequestTarget)}
	 */
	public final boolean isStopped()
	{
		return stopped;
	}

	/**
	 * Re-enables the timer if already stopped
	 *
	 * @param target
	 */
	public final void restart(final AjaxRequestTarget target)
	{
		if (isStopped())
		{
			stopped = false;
			headRendered = false;
			target.add(getComponent());
		}
	}

	/**
	 * Stops the timer
	 */
	public final void stop(final AjaxRequestTarget target)
	{
		stopped = true;
		String timeoutHandle = getTimeoutHandle();
		target.prependJavaScript("clearTimeout("+timeoutHandle+"); delete "+timeoutHandle+";");
	}
}
