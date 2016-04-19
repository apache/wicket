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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.lambda.WicketConsumer;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An ajax behavior that is attached to a certain client-side (usually javascript) event, such as
 * click, change, keydown, etc.
 * <p>
 * Example:
 * 
 * <pre>
 *         WebMarkupContainer div=new WebMarkupContainer(...);
 *         div.setOutputMarkupId(true);
 *         div.add(new AjaxEventBehavior(&quot;click&quot;) {
 *             protected void onEvent(AjaxRequestTarget target) {
 *                 System.out.println(&quot;ajax here!&quot;);
 *             }
 *         }
 * </pre>
 * 
 * This behavior will be linked to the <em>click</em> javascript event of the div WebMarkupContainer
 * represents, and so anytime a user clicks this div the {@link #onEvent(AjaxRequestTarget)} of the
 * behavior is invoked.
 *
 * <p>
 * <strong>Note</strong>: {@link #getEvent()} method cuts any <em>on</em> prefix from the given event name(s).
 * This is being done for easier migration of applications coming from Wicket 1.5.x where Wicket used
 * inline attributes like 'onclick=...'. If the application needs to use custom events with names starting with
 * <em>on</em> then {@link #getEvent()} should be overridden.
 * </p>
 *
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @see #onEvent(AjaxRequestTarget)
 */
public abstract class AjaxEventBehavior extends AbstractDefaultAjaxBehavior
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AjaxEventBehavior.class);

	private static final long serialVersionUID = 1L;

	private final String event;

	/**
	 * Construct.
	 * 
	 * @param event
	 *      the event this behavior will be attached to
	 */
	public AjaxEventBehavior(String event)
	{
		Args.notEmpty(event, "event");

		onCheckEvent(event);

		this.event = event;
	}

	@Override
	public void renderHead(final Component component, final IHeaderResponse response)
	{
		super.renderHead(component, response);

		if (component.isEnabledInHierarchy())
		{
			CharSequence js = getCallbackScript(component);

			response.render(OnDomReadyHeaderItem.forScript(js.toString()));
		}
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);

		String evt = getEvent();
		Checks.notEmpty(evt, "getEvent() should return non-empty event name(s)");
		attributes.setEventNames(evt);
	}

	/**
	 * 
	 * @param event
	 *      the event this behavior will be attached to
	 * @deprecated Wicket 8 Remove this method for Wicket 8.0.0
	 */
	@Deprecated
	protected void onCheckEvent(final String event)
	{
		if (event.startsWith("on"))
		{
			String shortName = event.substring(2);
			throw new IllegalArgumentException(
					String.format("Since version 6.0.0 Wicket uses JavaScript event registration so there is no need of the leading " +
									"'on' in the event name '%s'. Please use just '%s'. Wicket 8.x won't manipulate the provided event " +
									"names so the leading 'on' may break your application."
							, event, shortName));
		}

	}

	/**
	 * @return event
	 *      the event this behavior is attached to
	 */
	public String getEvent()
	{
		String[] splitEvents = event.split("\\s+");
		List<String> cleanedEvents = new ArrayList<>(splitEvents.length);
		for (String evt : splitEvents)
		{
			if (Strings.isEmpty(evt) == false)
			{
				cleanedEvents.add(evt);
			}
		}

		return Strings.join(" ", cleanedEvents);
	}

	@Override
	protected final void respond(final AjaxRequestTarget target)
	{
		onEvent(target);
	}

	/**
	 * Listener method for the ajax event
	 * 
	 * @param target
	 *      the current request handler
	 */
	protected abstract void onEvent(final AjaxRequestTarget target);

	/**
	 * Creates an {@link AjaxEventBehavior} based on lambda expressions
	 * 
	 * @param eventName
	 *            the event name
	 * @param onEvent
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxEventBehavior}
	 */
	public static AjaxEventBehavior onEvent(String eventName, WicketConsumer<AjaxRequestTarget> onEvent)
	{
		Args.notNull(onEvent, "onEvent");

		return new AjaxEventBehavior(eventName)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				onEvent.accept(target);
			}
		};
	}
}
