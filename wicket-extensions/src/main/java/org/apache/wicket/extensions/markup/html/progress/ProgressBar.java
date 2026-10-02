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
package org.apache.wicket.extensions.markup.html.progress;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A progress bar showing a percentage from 0 to 100, with its value as label. The bar is a native
 * {@code <progress>} element styled by CSS, so it needs no inline style and works under a strict
 * Content Security Policy. The filled part is striped and the stripes move, unless the user
 * prefers reduced motion; a finished bar, at 100, is solid.
 * <p>
 * A bar whose model holds {@code null} is indeterminate: it shows that something is going on
 * without saying how far it got, as moving stripes over the whole bar and without a label.
 * <p>
 * The colors come from the {@link org.apache.wicket.extensions.theme.Theme theme} the bar is
 * rendered in, if any.
 * <p>
 * The component is attached to a {@code <div>}. Its markup is available as a string from
 * {@link #markup(String)}, so the same bar can be produced elsewhere, for example as a
 * {@link org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.ProgressBarColumn
 * template} rendered in the browser. The look comes from {@link #CSS}.
 *
 * @since 11.0.0
 */
public class ProgressBar extends WebComponent
{
	private static final long serialVersionUID = 1L;

	/** The style sheet of the bar. */
	public static final ResourceReference CSS = new CssResourceReference(ProgressBar.class,
		"wicket-progress-bar.css");

	/** The CSS class of the bar's element. */
	public static final String CSS_CLASS = "wicket-progress-bar";

	/**
	 * @param id
	 *            component id
	 * @param model
	 *            the percentage, clamped to 0..100; {@code null} for an indeterminate bar
	 */
	public ProgressBar(String id, IModel<? extends Number> model)
	{
		super(id, model);
	}

	/**
	 * Returns the markup of a whole bar, the {@code value} written as is wherever the percentage
	 * goes: the {@code value} of the {@code <progress>} element and the label.
	 * <p>
	 * The value is not escaped. It has to be a number, or a placeholder of a template engine that
	 * produces one, such as {@code {{progress}}}.
	 *
	 * @param value
	 *            the percentage, or a placeholder for it
	 * @return the markup of the bar
	 */
	public static String markup(String value)
	{
		return "<div class=\"" + CSS_CLASS + "\">" + body(value) + "</div>";
	}

	/**
	 * @return the markup of a whole indeterminate bar
	 */
	public static String indeterminateMarkup()
	{
		return "<div class=\"" + CSS_CLASS + "\">" + indeterminateBody() + "</div>";
	}

	private static String indeterminateBody()
	{
		return "<progress class=\"" + CSS_CLASS + "-value\" max=\"100\"></progress>";
	}

	private static String body(String value)
	{
		return "<progress class=\"" + CSS_CLASS + "-value\" max=\"100\" value=\"" + value + "\">" +
			value + "%</progress><span class=\"" + CSS_CLASS + "-label\" aria-hidden=\"true\">" +
			value + "%</span>";
	}

	/**
	 * @return whether the bar is indeterminate, its model holding {@code null}
	 */
	public boolean isIndeterminate()
	{
		return getDefaultModelObject() == null;
	}

	/**
	 * @return the percentage shown, clamped to 0..100; 0 if the bar is indeterminate
	 */
	public int getPercentage()
	{
		Number value = (Number)getDefaultModelObject();
		int percentage = value != null ? Math.round(value.floatValue()) : 0;
		return Math.max(0, Math.min(100, percentage));
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		checkComponentTag(tag, "div");
		super.onComponentTag(tag);
		tag.append("class", CSS_CLASS, " ");
	}

	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		replaceComponentTagBody(markupStream, openTag,
			isIndeterminate() ? indeterminateBody() : body(String.valueOf(getPercentage())));
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(CSS));
	}
}
