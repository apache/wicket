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
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
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

	/**
	 * Is the timeout present in JavaScript already.
	 */
	private boolean hasTimeout = false;

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

		if (component.getRequestCycle().find(AjaxRequestTarget.class) == null)
		{
			// complete page is rendered, so timeout has to be rendered again
			hasTimeout = false;
		}

		if (isStopped() == false)
		{
			addTimeout(response);
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

		return String.format("Wicket.Timer.set('%s', function(){%s}, %d);",
				getComponent().getMarkupId(), js, updateInterval.getMilliseconds());
	}

	/**
	 * 
	 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#respond(AjaxRequestTarget)
	 */
	@Override
	protected final void respond(final AjaxRequestTarget target)
	{
		if (shouldTrigger())
		{
			onTimer(target);

			if (shouldTrigger())
			{
				// re-add timeout
				hasTimeout = false;

				addTimeout(target.getHeaderResponse());

				return;
			}
		}

		clearTimeout(target.getHeaderResponse());
	}

	/**
	 * Decides whether the timer behavior should render its JavaScript to re-trigger
	 * it after the update interval.
	 *
	 * @return {@code true} if the behavior is not stopped, it is enabled and still attached to
	 *      any component in the page or to the page itself
	 */
	protected boolean shouldTrigger()
	{
		return isStopped() == false &&
				isEnabled(getComponent()) &&
				(getComponent() instanceof Page || getComponent().findParent(Page.class) != null);
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
	 *            may be null
	 */
	public final void restart(final AjaxRequestTarget target)
	{
		if (stopped == true)
		{
			stopped = false;

			if (target != null)
			{
				addTimeout(target.getHeaderResponse());
			}
		}
	}

	private void addTimeout(IHeaderResponse headerResponse)
	{
		if (hasTimeout == false)
		{
			hasTimeout = true;

			headerResponse.render(
				OnLoadHeaderItem.forScript(getJsTimeoutCall(updateInterval)));
		}
	}

	private void clearTimeout(IHeaderResponse headerResponse)
	{
		if (hasTimeout)
		{
			hasTimeout = false;

			headerResponse.render(OnLoadHeaderItem.forScript("Wicket.Timer.clear('" + getComponent().getMarkupId() + "');"));
		}
	}

	/**
	 * Stops the timer.
	 * 
	 * @param target
	 *            may be null
	 */
	public final void stop(final AjaxRequestTarget target)
	{
		if (stopped == false)
		{
			stopped = true;

			if (target != null)
			{
				clearTimeout(target.getHeaderResponse());
			}
		}
	}

	@Override
	public void onRemove(Component component)
	{
		AjaxRequestTarget target = component.getRequestCycle().find(AjaxRequestTarget.class);
		if (target != null)
		{
			clearTimeout(target.getHeaderResponse());
		}
	}

	@Override
	protected void onUnbind()
	{
		AjaxRequestTarget target = getComponent().getRequestCycle().find(AjaxRequestTarget.class);
		if (target != null)
		{
			clearTimeout(target.getHeaderResponse());
		}
	}
}
