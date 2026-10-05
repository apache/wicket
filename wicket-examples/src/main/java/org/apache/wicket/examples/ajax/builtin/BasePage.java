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
package org.apache.wicket.examples.ajax.builtin;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.examples.ThemeChoice;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.extensions.markup.html.collapsible.CollapsiblePanel;
import org.apache.wicket.extensions.theme.Theme;
import org.apache.wicket.extensions.theme.ThemeBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;


/**
 * Base page for ajax example. The explanation is a {@link CollapsiblePanel} in the page's
 * {@link Theme}, which pages showing components of wicket-extensions let the user choose with
 * {@link #newThemeChoice(String, Component...)}.
 */
public class BasePage extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	private Theme theme = Theme.DEFAULT;

	@Override
	protected Class<? extends WicketExamplePage> getIndexPage()
	{
		return Index.class;
	}

	/**
	 * @return the theme of the page
	 */
	protected IModel<Theme> getThemeModel()
	{
		return LambdaModel.of(() -> theme, chosen -> theme = chosen);
	}

	/**
	 * @return a container in the page's theme, for the components of the example
	 */
	protected WebMarkupContainer newThemedContainer(String id)
	{
		WebMarkupContainer themed = new WebMarkupContainer(id);
		themed.setOutputMarkupId(true);
		themed.add(new ThemeBehavior(getThemeModel()));
		return themed;
	}

	/**
	 * @return a drop-down choosing the page's theme, repainting the explanation and the given
	 *         components
	 */
	protected ThemeChoice newThemeChoice(String id, Component... themed)
	{
		return new ThemeChoice(id, getThemeModel())
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onThemeChanged(AjaxRequestTarget target)
			{
				target.add(themed);
				target.add(BasePage.this.get("explanation"));
				BasePage.this.onThemeChanged(target);
			}
		};
	}

	/**
	 * Called after the user chose another theme with the drop-down of
	 * {@link #newThemeChoice(String, Component...)}, once the themed components are repainted.
	 * Does nothing by default.
	 */
	protected void onThemeChanged(AjaxRequestTarget target)
	{
	}

	@Override
	protected Component newExplanation(String id, IModel<String> explanation)
	{
		return new CollapsiblePanel(id, new ResourceModel("aboutThisExample"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Component newBody(String id)
			{
				return BasePage.super.newExplanation(id, explanation);
			}
		}.setRememberExpanded(true)
			.add(new ThemeBehavior(getThemeModel()))
			.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
			new CssResourceReference(BasePage.class, "BasePage.css")));
	}
}
