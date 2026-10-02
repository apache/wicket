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
package org.apache.wicket.extensions.markup.html.collapsible;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A title and a body the user can expand and collapse, collapsed by default.
 * <p>
 * The panel renders the native {@code <details>} and {@code <summary>} elements: expanding and
 * collapsing happens in the browser, without JavaScript, is keyboard accessible and needs no
 * inline script or style, so it works under a strict Content Security Policy. The look comes from
 * {@link #CSS}. The color of the body's text is the CSS custom property
 * {@code --wicket-collapsible-text} if it is set, for example on the page, else the color of the
 * panel's parent.
 * <p>
 * By default the server does not learn when the user expands or collapses the panel, and renders
 * it again in the state of {@link #setExpanded(boolean)}. With
 * {@link #setRememberExpanded(boolean)} every toggle is reported via Ajax, so the panel keeps the
 * user's choice when it or its page is rendered again.
 * <p>
 * The title is escaped. The body is the component returned by {@link #newBody(String)}, for
 * example a label or a panel.
 *
 * @since 11.0.0
 */
public abstract class CollapsiblePanel extends Panel
{
	private static final long serialVersionUID = 1L;

	/** The style sheet of the panel. */
	public static final ResourceReference CSS = new CssResourceReference(CollapsiblePanel.class,
		"wicket-collapsible.css");

	private static final String OPEN_PARAMETER = "open";

	private final WebMarkupContainer details;

	private boolean expanded;

	private boolean rememberExpanded;

	/**
	 * @param id
	 *            component id
	 * @param title
	 *            the title, always shown, which the user clicks to expand or collapse the body
	 */
	public CollapsiblePanel(String id, IModel<String> title)
	{
		super(id);

		details = new WebMarkupContainer("details");
		details.setOutputMarkupId(true);
		details.add(AttributeModifier.replace("open", () -> expanded ? "open" : null));
		add(details);
		details.add(new Label("title", title));
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		details.add(newBody("body"));
		details.add(new ToggleBehavior());
	}

	/**
	 * Creates the body of the panel, shown when it is expanded.
	 *
	 * @param id
	 *            the id the body must have
	 * @return the body
	 */
	protected abstract Component newBody(String id);

	/**
	 * @return whether the panel is rendered expanded, {@code false} by default
	 */
	public boolean isExpanded()
	{
		return expanded;
	}

	/**
	 * @param expanded
	 *            whether the panel is rendered expanded
	 * @return {@code this}
	 */
	public CollapsiblePanel setExpanded(boolean expanded)
	{
		this.expanded = expanded;
		return this;
	}

	/**
	 * @return whether the browser reports every toggle to the server, {@code false} by default
	 */
	public boolean isRememberExpanded()
	{
		return rememberExpanded;
	}

	/**
	 * @param rememberExpanded
	 *            whether the browser reports every toggle to the server via Ajax, updating
	 *            {@link #isExpanded()}
	 * @return {@code this}
	 */
	public CollapsiblePanel setRememberExpanded(boolean rememberExpanded)
	{
		this.rememberExpanded = rememberExpanded;
		return this;
	}

	/**
	 * Called when the user expanded or collapsed the panel and {@link #isRememberExpanded()} is
	 * on. Does nothing by default.
	 *
	 * @param target
	 *            the Ajax request target
	 */
	protected void onToggle(AjaxRequestTarget target)
	{
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(CSS));
	}

	private class ToggleBehavior extends AjaxEventBehavior
	{
		private static final long serialVersionUID = 1L;

		ToggleBehavior()
		{
			super("toggle");
		}

		@Override
		public boolean isEnabled(Component component)
		{
			return rememberExpanded && super.isEnabled(component);
		}

		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
		{
			super.updateAjaxAttributes(attributes);
			attributes.getDynamicExtraParameters()
				.add("return {'" + OPEN_PARAMETER + "': Wicket.$(attrs.c).open};");
		}

		@Override
		protected void onEvent(AjaxRequestTarget target)
		{
			expanded = getRequest().getRequestParameters()
				.getParameterValue(OPEN_PARAMETER)
				.toBoolean(expanded);
			onToggle(target);
		}
	}
}
