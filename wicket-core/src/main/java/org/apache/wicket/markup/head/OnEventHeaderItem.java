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
package org.apache.wicket.markup.head;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.settings.JavaScriptLibrarySettings;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.AttributeMap;

/**
 * {@link HeaderItem} for event triggered scripts.
 *
 * @author papegaaij
 */
public class OnEventHeaderItem extends AbstractCspHeaderItem
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a {@link OnEventHeaderItem} for the given parameters.
	 *
	 * @param literalTarget
	 *            The target of the event handler, for example 'window' or 'document'. Note that
	 *            this parameter is a literal and will be rendered unquoted.
	 * @param event
	 *            The event itself, for example 'click'.
	 * @param javaScript
	 *            The script to execute on the event.
	 *
	 * @return A newly created {@link OnEventHeaderItem}.
	 * @see #forComponent(Component, String, CharSequence)
	 * @see #forMarkupId(String, String, CharSequence)
	 */
	public static OnEventHeaderItem forScript(String literalTarget, String event,
			CharSequence javaScript)
	{
		return new OnEventHeaderItem(literalTarget, event, javaScript);
	}

	/**
	 * Creates a {@link OnEventHeaderItem} for the given parameters.
	 *
	 * @param target
	 *            The target component of the event handler.
	 * @param event
	 *            The event itself, for example 'click'.
	 * @param javaScript
	 *            The script to execute on the event.
	 *
	 * @return A newly created {@link OnEventHeaderItem}.
	 */
	public static OnEventHeaderItem forComponent(Component target, String event,
			CharSequence javaScript)
	{
		return forMarkupId(target.getMarkupId(), event, javaScript);
	}

	/**
	 * Creates a {@link OnEventHeaderItem} for the given parameters.
	 *
	 * @param id
	 *            The id of the component to bind the handler to.
	 * @param event
	 *            The event itself, for example 'click'.
	 * @param javaScript
	 *            The script to execute on the event.
	 *
	 * @return A newly created {@link OnEventHeaderItem}.
	 */
	public static OnEventHeaderItem forMarkupId(String id, String event, CharSequence javaScript)
	{
		return forScript("'" + id + "'", event, javaScript);
	}

	private final String target;

	private final String event;

	private final CharSequence javaScript;

	/**
	 * Constructor.
	 *
	 * The JavaScript should be provided by overloaded #getJavaScript
	 *
	 * @param target
	 * @param event
	 */
	public OnEventHeaderItem(String target, String event)
	{
		this(target, event, null);
	}

	/**
	 * Construct.
	 *
	 * @param target
	 * @param event
	 * @param javaScript
	 */
	public OnEventHeaderItem(String target, String event, CharSequence javaScript)
	{
		this.target = Args.notEmpty(target, "target");

		Args.notEmpty(event, "event");
		event = event.toLowerCase(Locale.ROOT);
		this.event = event;
		this.javaScript = javaScript;
	}

	/**
	 * @return The target of the event handler, for example 'window' or 'document'.
	 */
	public String getTarget()
	{
		return target;
	}

	/**
	 * @return The event itself, for example 'onclick'.
	 */
	public String getEvent()
	{
		return event;
	}

	/**
	 * @return The script to execute on the event.
	 */
	public CharSequence getJavaScript()
	{
		return javaScript;
	}

	@Override
	public void render(Response response)
	{
		if (Strings.isEmpty(getJavaScript()) == false)
		{
			AttributeMap attributes = new AttributeMap();
			attributes.putAttribute(JavaScriptUtils.ATTR_TYPE, "text/javascript");
			attributes.putAttribute(JavaScriptUtils.ATTR_CSP_NONCE, getNonce());
			JavaScriptUtils.writeInlineScript(response, getCompleteJavaScript(), attributes);
		}
	}

	/**
	 * @return The JavaScript that registers the event handler.
	 */
	public CharSequence getCompleteJavaScript()
	{
		StringBuilder result = new StringBuilder();
		result.append("Wicket.Event.add(")
				.append(getTarget())
				.append(", \'")
				.append(getEvent())
				.append("\', function(event) { ")
				.append(getJavaScript())
				.append(";});");
		return result;
	}

	@Override
	public Iterable<?> getRenderTokens()
	{
		return Collections.singletonList("javascript-event-" + getTarget() + "-" + getEvent() +
			"-" + getJavaScript());
	}

	@Override
	public String toString()
	{
		return "OnEventHeaderItem(" + getTarget() + ", '" + getEvent() + "', '" + getJavaScript() + "')";
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(target, event, javaScript);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OnEventHeaderItem that = (OnEventHeaderItem) o;
		return Objects.equals(target, that.target) &&
				Objects.equals(event, that.event) &&
				Objects.equals(javaScript, that.javaScript);
	}

	@Override
	public List<HeaderItem> getDependencies()
	{
		JavaScriptLibrarySettings ajaxSettings = Application.get().getJavaScriptLibrarySettings();
		ResourceReference wicketAjaxReference = ajaxSettings.getWicketAjaxReference();
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(wicketAjaxReference));
		return dependencies;
	}
}
