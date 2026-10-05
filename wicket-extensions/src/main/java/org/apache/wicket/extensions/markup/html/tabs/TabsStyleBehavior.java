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
package org.apache.wicket.extensions.markup.html.tabs;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;

/**
 * Gives a {@link TabbedPanel} one of the looks of {@link TabsStyle}, by a CSS class only, so it
 * needs no inline style. The look comes from a model, so re-rendering the panel switches it.
 * Without this behavior a tabbed panel has no style sheet of its own and looks as the
 * application's CSS makes it look.
 * <p>
 * The looks mark the selected tab by the CSS class {@code selected}, the default of
 * {@link TabbedPanel#getSelectedTabCssClass()}; a panel configured with another class shows no
 * selected tab.
 *
 * @since 11.0.0
 */
public class TabsStyleBehavior extends Behavior
{
	private static final long serialVersionUID = 1L;

	/** The style sheet of the looks. */
	public static final ResourceReference CSS = new CssResourceReference(TabsStyleBehavior.class,
		"wicket-tabs.css");

	private final IModel<TabsStyle> style;

	/**
	 * @param style
	 *            the look of the tabs
	 */
	public TabsStyleBehavior(TabsStyle style)
	{
		this(Model.of(Args.notNull(style, "style")));
	}

	/**
	 * @param style
	 *            the look of the tabs; {@code null} as its object for none
	 */
	public TabsStyleBehavior(IModel<TabsStyle> style)
	{
		this.style = Args.notNull(style, "style");
	}

	@Override
	public void onComponentTag(Component component, ComponentTag tag)
	{
		TabsStyle current = style.getObject();
		if (current != null)
		{
			tag.append("class", "wicket-tabs " + current.getCssClass(), " ");
		}
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		response.render(CssHeaderItem.forReference(CSS));
	}

	@Override
	public void detach(Component component)
	{
		style.detach();
	}
}
