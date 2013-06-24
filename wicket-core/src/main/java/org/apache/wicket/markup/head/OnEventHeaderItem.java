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

import org.apache.wicket.Application;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.settings.IJavaScriptLibrarySettings;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.core.util.string.JavaScriptUtils;

/**
 * {@link HeaderItem} for event triggered scripts.
 * 
 * @author papegaaij
 */
public class OnEventHeaderItem extends HeaderItem
{
	/**
	 * Creates a {@link OnEventHeaderItem} for the given parameters.
	 * 
	 * @param target
	 *            The target of the event handler, for example 'window' or 'document'.
	 * @param event
	 *            The event itself, for example 'onclick'.
	 * @param javaScript
	 *            The script to execute on the event.
	 * 
	 * @return A newly created {@link OnEventHeaderItem}.
	 */
	public static OnEventHeaderItem forScript(String target, String event, CharSequence javaScript)
	{
		return new OnEventHeaderItem(target, event, javaScript);
	}

	private final String target;
	private final String event;
	private final CharSequence javaScript;

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
		this.event = Args.notEmpty(event, "event");
		this.javaScript = Args.notEmpty(javaScript, "javaScript");
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
		JavaScriptUtils.writeJavaScript(response, getCompleteJavaScript());
	}

	/**
	 * @return The JavaScript that registers the event handler.
	 */
	public CharSequence getCompleteJavaScript()
	{
		StringBuilder result = new StringBuilder();
		result.append("Wicket.Event.add(")
				.append(getTarget())
				.append(", \"")
				.append(getEvent())
				.append("\", function(event) { ")
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
		return getTarget().hashCode() ^ getEvent().hashCode() ^ getJavaScript().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof OnEventHeaderItem)
		{
			OnEventHeaderItem other = (OnEventHeaderItem)obj;
			return other.getTarget().equals(getTarget()) && other.getEvent().equals(getEvent()) &&
				other.getJavaScript().equals(getJavaScript());
		}
		return false;
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies()
	{
		IJavaScriptLibrarySettings ajaxSettings = Application.get().getJavaScriptLibrarySettings();
		ResourceReference wicketEventReference = ajaxSettings.getWicketEventReference();
		return Collections.singletonList(JavaScriptHeaderItem.forReference(wicketEventReference));
	}
}
