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
import org.apache.wicket.IClusterable;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;

/**
 * An ajax behavior that is attached to a certain client-side (usually javascript) event, such as
 * onClick, onChange, onKeyDown, etc.
 * <p>
 * Example:
 * 
 * <pre>
 *         WebMarkupContainer div=new WebMarkupContainer(...);
 *         div.setOutputMarkupId(true);
 *         div.add(new AjaxEventBehavior(&quot;onclick&quot;) {
 *             protected void onEvent(AjaxRequestTarget target) {
 *                 System.out.println(&quot;ajax here!&quot;);
 *             }
 *         }
 * </pre>
 * 
 * This behavior will be linked to the onclick javascript event of the div WebMarkupContainer
 * represents, and so anytime a user clicks this div the {@link #onEvent(AjaxRequestTarget)} of the
 * behavior is invoked.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AjaxEventBehavior extends AbstractDefaultAjaxBehavior
{
	private static long sequence = 0;

	private static final long serialVersionUID = 1L;

	private final String event;


	private ThrottlingSettings throttlingSettings;

	/**
	 * Construct.
	 * 
	 * @param event
	 *            event this behavior will be attached to
	 */
	public AjaxEventBehavior(final String event)
	{
		if (Strings.isEmpty(event))
		{
			throw new IllegalArgumentException("argument [event] cannot be null or empty");
		}

		onCheckEvent(event);

		this.event = event;
	}

	/**
	 * Sets the throttle delay for this behavior. Throttled behaviors only execute once within the
	 * given delay even though they are triggered multiple times.
	 * <p>
	 * For example, this is useful when attaching this behavior to the onkeypress event. It is not
	 * desirable to have an ajax call made every time the user types so we throttle that call to a
	 * desirable delay, such as once per second. This gives us a near real time ability to provide
	 * feedback without overloading the server with ajax calls.
	 * 
	 * 
	 * @param throttleDelay
	 *            throttle delay
	 * @return this for chaining
	 */
	public final AjaxEventBehavior setThrottleDelay(Duration throttleDelay)
	{
		throttlingSettings = new ThrottlingSettings("th" + (++sequence), throttleDelay);
		return this;
	}

	/**
	 * 
	 * @see org.apache.wicket.behavior.AbstractAjaxBehavior#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);

		// only add the event handler when the component is enabled.
		Component myComponent = getComponent();
		if (myComponent.isEnabledInHierarchy())
		{
			tag.put(event, escapeAttribute(getEventHandler()));
		}
	}
	
	private CharSequence escapeAttribute(final CharSequence attr)
	{
		if(null == attr)
		{
			return null;
		}
		CharSequence escaped = Strings.escapeMarkup(attr.toString());
		// No need to escape the apostrophe; it just clutters the markup
		return Strings.replaceAll(escaped, "&#039;", "'");
	}

	/**
	 * 
	 * @return event handler
	 */
	protected CharSequence getEventHandler()
	{
		CharSequence handler = getCallbackScript();
		if (event.equalsIgnoreCase("href"))
		{
			handler = "javascript:" + handler;
		}
		return handler;
	}

	@Override
	protected CharSequence generateCallbackScript(CharSequence partialCall)
	{
		CharSequence script = super.generateCallbackScript(partialCall);
		final ThrottlingSettings ts = throttlingSettings;

		if (ts != null)
		{
			script = AbstractDefaultAjaxBehavior.throttleScript(script, ts.getId(), ts.getDelay());
		}
		return script;
	}

	/**
	 * 
	 * @param event
	 */
	protected void onCheckEvent(final String event)
	{
	}

	/**
	 * 
	 * @return event
	 */
	public final String getEvent()
	{
		return event;
	}

	/**
	 * 
	 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#respond(org.apache.wicket.ajax.AjaxRequestTarget)
	 */
	@Override
	protected final void respond(final AjaxRequestTarget target)
	{
		onEvent(target);
	}

	/**
	 * Listener method for the ajax event
	 * 
	 * @param target
	 */
	protected abstract void onEvent(final AjaxRequestTarget target);


	/**
	 * Class to keep track of throttling settings.
	 * 
	 * @author ivaynberg
	 */
	private static class ThrottlingSettings implements IClusterable
	{
		private static final long serialVersionUID = 1L;

		private final Duration delay;
		private final String id;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            throttle id
		 * @param delay
		 *            throttle delay
		 */
		public ThrottlingSettings(final String id, final Duration delay)
		{
			super();
			this.id = id;
			this.delay = delay;
		}

		/**
		 * @return throttle delay
		 */
		public Duration getDelay()
		{
			return delay;
		}

		/**
		 * @return throttle id
		 */
		public String getId()
		{
			return id;
		}


	}
}
