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
package org.apache.wicket.extensions.theme;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Args;

/**
 * Applies a {@link Theme} to a component and everything inside it, by adding the theme's CSS
 * class to the component's tag and contributing {@link Theme#CSS}.
 * <p>
 * The theme is read from a model each time the component renders, so re-rendering the component,
 * for example via Ajax, switches it.
 *
 * @since 11.0.0
 */
public class ThemeBehavior extends Behavior
{
	private static final long serialVersionUID = 1L;

	private final IModel<Theme> theme;

	/**
	 * @param theme
	 *            the theme
	 */
	public ThemeBehavior(Theme theme)
	{
		this(Model.of(Args.notNull(theme, "theme")));
	}

	/**
	 * @param theme
	 *            the theme, none if the model holds {@code null}
	 */
	public ThemeBehavior(IModel<Theme> theme)
	{
		this.theme = Args.notNull(theme, "theme");
	}

	@Override
	public void onComponentTag(Component component, ComponentTag tag)
	{
		Theme current = theme.getObject();
		if (current != null)
		{
			tag.append("class", Theme.MARKER_CSS_CLASS + " " + current.getCssClass(), " ");
		}
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		response.render(CssHeaderItem.forReference(Theme.CSS));
	}

	@Override
	public void detach(Component component)
	{
		theme.detach();
	}
}
