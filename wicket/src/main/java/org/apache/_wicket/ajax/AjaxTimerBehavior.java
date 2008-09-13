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
package org.apache._wicket.ajax;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.util.time.Duration;

/**
 * A behavior that generates an AJAX update callback at a regular interval.
 * 
 * @author Matej Knopp
 */
public abstract class AjaxTimerBehavior extends AjaxBehavior
{
	private static final long serialVersionUID = 1L;

	/** The update interval */
	private Duration updateInterval;

	/** Indicates whether the behavior is active or stopped */
	private boolean stopped = false;

	/** Flag to prevent multiple script rendering in one request */
	private boolean headRendered = false;

	/**
	 * Construct.
	 * 
	 * @param updateInterval
	 *            Duration between AJAX callbacks
	 */
	public AjaxTimerBehavior(final Duration updateInterval)
	{
		if (updateInterval == null || updateInterval.getMilliseconds() <= 0)
		{
			throw new IllegalArgumentException("Invalid update interval");
		}
		this.updateInterval = updateInterval;
	}

	/**
	 * Sets the update interval duration. This method should only be called within the
	 * {@link #onTimer(AjaxRequestTarget)} method.
	 * 
	 * @param updateInterval
	 * @return <code>this</code>
	 */
	protected final AjaxTimerBehavior setUpdateInterval(Duration updateInterval)
	{
		if (updateInterval == null || updateInterval.getMilliseconds() <= 0)
		{
			throw new IllegalArgumentException("Invalid update interval");
		}
		this.updateInterval = updateInterval;
		return this;
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

	private Component getComponent()
	{
		if (getBoundComponents().isEmpty())
		{
			return null;
		}
		else
		{
			return getBoundComponents().iterator().next();
		}
	}

	/**
	 * Returns unique Id for this behavior. The id will be used as RequestQueueItem token and also
	 * to cancel pending timeout on client when {@link #stop()} is called.
	 * 
	 * @return unique id for this behavior
	 */
	protected String getId()
	{
		Component component = getComponent();
		int index = component.getBehaviors().indexOf(this);
		StringBuilder b = new StringBuilder();
		b.append(component instanceof Page ? "_page_" : component.getMarkupId());
		b.append("___");
		b.append(index);
		return b.toString();
	}

	@Override
	protected boolean allowBindToMultipleComponents()
	{
		return false;
	}

	private String renderSetTimeoutJavascript()
	{
		String attrs = renderAttributes(getComponent());

		StringBuilder b = new StringBuilder();
		b.append("W.timerManager.setTimeout(");
		b.append(updateInterval.getMilliseconds());
		b.append(",");
		b.append(attrs);
		b.append(");");

		return b.toString();
	}

	private String renderRemoveTimeoutJavascript()
	{
		String attrs = renderAttributes(getComponent());

		StringBuilder b = new StringBuilder();
		b.append("W.timerManager.removeTimeout(");
		b.append(attrs);
		b.append(");");

		return b.toString();
	}

	private void renderAjax(AjaxRequestTarget target)
	{
		if (!headRendered)
		{
			if (stopped)
			{
				target.appendJavascript(renderRemoveTimeoutJavascript());
			}
			else
			{
				target.appendJavascript(renderSetTimeoutJavascript());
			}
			headRendered = true;
		}
	}

	/**
	 * Returns whether this behavior has been stopped.
	 * 
	 * @see #stop()
	 * @see #resume()
	 * 
	 * @return <code>true<code> if the behavior has been stopped, <code>false</code> otherwise.
	 */
	public boolean isStopped()
	{
		return stopped;
	}

	/**
	 * Stops this behavior. The {@link #onTimer(AjaxRequestTarget)} method will not be called
	 * until the timer is enabled again.
	 * 
	 * @see #isStopped()
	 * @see #resume()
	 */
	public void stop()
	{
		if (stopped == false)
		{
			stopped = true;
			AjaxRequestTarget target = AjaxRequestTarget.get();
			if (target != null)
			{
				renderAjax(target);
			}
		}
	}

	/**
	 * Resumes stopped behavior.
	 * 
	 * @see #isStopped()
	 * @see #stop()
	 */
	public void resume()
	{
		if (stopped == true)
		{
			stopped = false;
			AjaxRequestTarget target = AjaxRequestTarget.get();
			if (target != null)
			{
				renderAjax(target);
			}
		}
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		// only set the timeout on regular (non-ajax) request when the behavior is enabled 
		AjaxRequestTarget target = AjaxRequestTarget.get();
		if (target == null && stopped == false)
		{
			response.renderOnDomReadyJavascript(renderSetTimeoutJavascript());
		}
	}

	@Override
	public void detach(Component component)
	{
		super.detach(component);

		headRendered = false;
	}

	abstract protected void onTimer(AjaxRequestTarget target);

	public final void respond(AjaxRequestTarget target)
	{
		onTimer(target);
		renderAjax(target);
	}

	@Override
	protected void updateAttributes(AjaxRequestAttributes attributes, Component component)
	{
		attributes.setRemovePrevious(true);
		attributes.setToken(getId());
	}	
}
