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
package org.apache.wicket.extensions.markup.html.repeater.data.table.filter;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.Strings;

/**
 * A form with filter-related special functionality for its form components.
 * 
 * <p>
 * This form uses an invisible button to be able to submit when the user presses the <em>ENTER</em>
 * key. If there is a need to add an explicit
 * {@link org.apache.wicket.markup.html.form.IFormSubmittingComponent} to this form then
 * {@link Form#setDefaultButton(org.apache.wicket.markup.html.form.IFormSubmittingComponent)} should
 * be used to specify this custom submitting component.
 * </p>
 * 
 * @param <T>
 *            type of filter state object
 * @author igor
 */
public class FilterForm<T> extends Form<T>
{
	private static final long serialVersionUID = 1L;

	private static final ResourceReference JS = new JavaScriptResourceReference(FilterForm.class,
		"wicket-filterform.js");

	private final IFilterStateLocator<T> locator;

	/**
	 * @param id
	 *            component id
	 * @param locator
	 *            filter state locator
	 */
	public FilterForm(final String id, final IFilterStateLocator<T> locator)
	{
		super(id, new FilterStateModel<T>(locator));

		this.locator = locator;
	}

	@Override
	public void renderHead(final IHeaderResponse response)
	{
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(JS));

		response.render(OnLoadHeaderItem.forScript(String.format(
			"Wicket.FilterForm.restore('%s');", getFocusTrackerFieldCssId())));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		super.onComponentTagBody(markupStream, openTag);

		String id = Strings.escapeMarkup(getFocusTrackerFieldCssId()).toString();
		String value = getRequest().getPostParameters().getParameterValue(id).toString("");
		getResponse().write(
			String.format(
				"<div style='position: absolute; left: -9999px; width: 1px; height: 1px;'><input type='hidden' name='%s' id='%s' value='%s'/><input type='submit'/></div>",
				id, id, value));
	}

	/**
	 * @return css id of the hidden form input that keeps track of the focused input field
	 */
	public final String getFocusTrackerFieldCssId()
	{
		return getMarkupId() + "focus";
	}

	/**
	 * @return IFilterStateLocator passed to this form
	 */
	public final IFilterStateLocator<T> getStateLocator()
	{
		return locator;
	}

	/**
	 * Adds behavior to the form component to allow this form to keep track of the component's focus
	 * which will be restored after a form submit.
	 * 
	 * @param fc
	 *            form component
	 */
	public final void enableFocusTracking(final FormComponent<?> fc)
	{
		fc.add(new Behavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void bind(Component component)
			{
				super.bind(component);
				component.setOutputMarkupId(true);
			}

			@Override
			public void onComponentTag(final Component component, final ComponentTag tag)
			{
				tag.put("onfocus", getFocusTrackingHandler(component));

				super.onComponentTag(component, tag);
			}
		});
	}

	/**
	 * Returns the javascript focus handler necessary to notify the form of focus tracking changes
	 * on the component
	 * 
	 * Useful when components want to participate in focus tracking but want to add the handler
	 * their own way.
	 * 
	 * A unique css id is required on the form component for focus tracking to work.
	 * 
	 * @param component
	 *            component to
	 * @return the javascript focus handler necessary to notify the form of focus tracking changes
	 *         on the component
	 */
	public final String getFocusTrackingHandler(final Component component)
	{
		return String.format("Wicket.FilterForm.focused(this, '%s');", getFocusTrackerFieldCssId());
	}
}
