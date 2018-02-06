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
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Duration;
import org.danekja.java.util.function.serializable.SerializableConsumer;

/**
 * A behavior that generates an AJAX update callback at a regular interval.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 * @see #onTimer(AjaxRequestTarget)
 * @see #restart(IPartialPageRequestHandler)
 * @see #stop(IPartialPageRequestHandler)
 */
public abstract class AbstractAjaxTimerBehavior extends AbstractDefaultAjaxBehavior
{
	private static final long serialVersionUID = 1L;

	/** The update interval */
	private Duration updateInterval;

	private boolean stopped = false;
	
	/**
	 * Id of timer in JavaScript.
	 */
	private String timerId;

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

		if (isStopped() == false)
		{
			setTimeout(response);
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#respond(AjaxRequestTarget)
	 */
	@Override
	protected final void respond(final AjaxRequestTarget target)
	{
		// timerId is no longer valid after timer has triggered
		timerId = null;
		
		if (shouldTrigger())
		{
			onTimer(target);

			if (shouldTrigger())
			{
				setTimeout(target.getHeaderResponse());
			}
		}
	}

	/**
	 * Decides whether the timer behavior should render its JavaScript to re-trigger it after the
	 * update interval.
	 *
	 * @return {@code true} if the behavior is not stopped, it is enabled and still attached to any
	 *         component in the page or to the page itself
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
	 * @return {@code true} if has been stopped via {@link #stop(IPartialPageRequestHandler)}
	 */
	public final boolean isStopped()
	{
		return stopped;
	}

	/**
	 * Restart the timer.
	 * 
	 * @param target
	 *            may be null
	 */
	public final void restart(final IPartialPageRequestHandler target)
	{
		stopped = false;

		if (target != null)
		{
			setTimeout(target.getHeaderResponse());
		}
	}

	/**
	 * Create an identifier for the JavaScript timer.
	 * <p>
	 * Note: The identifier must not change as long as this behavior is attached to a component!
	 * 
	 * @return creates an id based on {@link Component#getMarkupId()} and
	 *         {@link Component#getBehaviorById(int)} by default
	 */
	protected String getTimerId()
	{
		Component component = getComponent();

		return component.getMarkupId() + "." + component.getBehaviorId(this);
	}

	/**
	 * Set the timeout on the given {@link IHeaderResponse}. Implementation note:
	 * <p>
	 * {@link #respond(AjaxRequestTarget)} might set the timer once and
	 * {@link #renderHead(Component, IHeaderResponse)} a second time successively, if the attached
	 * component is re-rendered on the same {@link AjaxRequestTarget}.
	 * <p>
	 * But rendering of the component might <em>not</em> actually happen on the same {@link AjaxRequestTarget},
	 * e.g. when a redirect to a full page-render is scheduled. Thus this method <em>always</em> sets the timeout 
	 * and in the former case {@link AjaxRequestTarget} will take care of executing one of the
	 * two {@link OnLoadHeaderItem}s only.
	 * 
	 * @param headerResponse
	 */
	private void setTimeout(IHeaderResponse headerResponse)
	{
		CharSequence js = getCallbackScript();

		// remember id to be able to clear it later
		timerId = getTimerId();

		headerResponse.render(
			OnLoadHeaderItem.forScript(String.format("Wicket.Timer.set('%s', function(){%s}, %d);",
				timerId, js, updateInterval.getMilliseconds())));
	}

	private void clearTimeout(IHeaderResponse headerResponse)
	{
		if (timerId != null)
		{
			headerResponse
				.render(OnLoadHeaderItem.forScript("Wicket.Timer.clear('" + timerId + "');"));

			timerId = null;
		}
	}

	/**
	 * Stops the timer.
	 * 
	 * @param target
	 *            may be null
	 */
	public final void stop(final IPartialPageRequestHandler target)
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
		component.getRequestCycle().find(IPartialPageRequestHandler.class).ifPresent(target -> clearTimeout(target.getHeaderResponse()));
	}

	@Override
	protected void onUnbind()
	{
		Component component = getComponent();
		
		component.getRequestCycle().find(IPartialPageRequestHandler.class).ifPresent(target -> clearTimeout(target.getHeaderResponse()));
	}

	/**
	 * Creates an {@link AbstractAjaxTimerBehavior} based on lambda expressions
	 *
	 * @param interval
	 *            the interval the timer
	 * @param onTimer
	 *            the consumer which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AbstractAjaxTimerBehavior}
	 */
	public static AbstractAjaxTimerBehavior onTimer(Duration interval, SerializableConsumer<AjaxRequestTarget> onTimer)
	{
		Args.notNull(onTimer, "onTimer");

		return new AbstractAjaxTimerBehavior(interval)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onTimer(AjaxRequestTarget target)
			{
				onTimer.accept(target);
			}
		};
	}
}
