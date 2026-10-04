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
package org.apache.wicket.extensions.markup.html.clipboard;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;

import com.github.openjson.JSONObject;

/**
 * Copies a text to the clipboard when the user clicks, or double clicks, the component, and
 * briefly shows a check mark in it; screen readers announce the copy.
 * <p>
 * The behavior writes the text into the component's {@code data-wicket-copy} attribute, escaped
 * like any attribute value, and the trigger into {@code data-wicket-copy-on}. The copying is done
 * in the browser by {@code Wicket.Clipboard} from {@link #JS}, which copies for every element
 * carrying these attributes, also for markup rendered in the browser, for example by a
 * DynamicDataTable column template; such markup only needs the header items of
 * {@link #renderHeadItems(IHeaderResponse)}. {@code Wicket.Clipboard.copy(text, element)}
 * copies from any script. The browser lets a page write to the clipboard only in a secure context
 * (HTTPS or localhost) and in response to a user action.
 *
 * @since 11.0.0
 */
public class ClipboardCopyBehavior extends Behavior
{
	private static final long serialVersionUID = 1L;

	/** The script copying to the clipboard, {@code Wicket.Clipboard}. */
	public static final ResourceReference JS = new JavaScriptResourceReference(
		ClipboardCopyBehavior.class, "wicket-clipboard.js");

	/** The style sheet of the check mark. */
	public static final ResourceReference CSS = new CssResourceReference(
		ClipboardCopyBehavior.class, "wicket-clipboard.css");

	/**
	 * What makes the component copy.
	 */
	public enum Trigger
	{
		/** A click. */
		CLICK("click"),

		/** A double click, leaving single clicks to select text. */
		DOUBLE_CLICK("dblclick");

		private final String event;

		Trigger(String event)
		{
			this.event = event;
		}

		/**
		 * @return the name of the DOM event
		 */
		public String getEvent()
		{
			return event;
		}
	}

	private final IModel<String> text;

	private Trigger trigger = Trigger.CLICK;

	/**
	 * @param text
	 *            the text to copy
	 */
	public ClipboardCopyBehavior(IModel<String> text)
	{
		this.text = Args.notNull(text, "text");
	}

	/**
	 * @param trigger
	 *            what makes the component copy, a click by default
	 * @return {@code this}
	 */
	public ClipboardCopyBehavior setTrigger(Trigger trigger)
	{
		this.trigger = Args.notNull(trigger, "trigger");
		return this;
	}

	/**
	 * @return what makes the component copy
	 */
	public Trigger getTrigger()
	{
		return trigger;
	}

	@Override
	public void onComponentTag(Component component, ComponentTag tag)
	{
		super.onComponentTag(component, tag);
		String value = text.getObject();
		tag.put("data-wicket-copy", value != null ? value : "");
		tag.put("data-wicket-copy-on", trigger.getEvent());
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);
		renderHeadItems(response);
	}

	/**
	 * Renders what copying needs in the browser: {@link #JS}, {@link #CSS} and the localized
	 * message read to screen readers after a copy, from the resource
	 * {@code ClipboardCopyBehavior.copied}.
	 *
	 * @param response
	 *            the header response
	 */
	public static void renderHeadItems(IHeaderResponse response)
	{
		response.render(JavaScriptHeaderItem.forReference(JS));
		response.render(CssHeaderItem.forReference(CSS));
		String copied = Application.get().getResourceSettings().getLocalizer().getString(
			"ClipboardCopyBehavior.copied", null, "Copied");
		response.render(OnDomReadyHeaderItem.forScript(
			"Wicket.Clipboard.copiedMessage = " + JSONObject.quote(copied) + ";"));
	}

	@Override
	public void detach(Component component)
	{
		text.detach();
		super.detach(component);
	}
}
