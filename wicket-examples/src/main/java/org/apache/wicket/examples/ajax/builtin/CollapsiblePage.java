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
import org.apache.wicket.extensions.markup.html.collapsible.CollapsiblePanel;
import org.apache.wicket.extensions.theme.Theme;
import org.apache.wicket.extensions.theme.ThemeBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;

/**
 * {@link CollapsiblePanel}s: collapsed, expanded, and one remembering its state on the server,
 * in a theme the user chooses.
 */
public class CollapsiblePage extends BasePage
{
	private static final long serialVersionUID = 1L;

	private Theme theme = Theme.DEFAULT;

	private String name;

	private int toggles;

	/**
	 * Constructor.
	 */
	public CollapsiblePage()
	{
		WebMarkupContainer themed = new WebMarkupContainer("themed");
		themed.setOutputMarkupId(true);
		themed.add(new ThemeBehavior(new PropertyModel<>(this, "theme")));
		add(themed);
		themed.add(new ThemeChoice("theme", new PropertyModel<>(this, "theme"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onThemeChanged(AjaxRequestTarget target)
			{
				target.add(themed);
			}
		});

		themed.add(textPanel("collapsed"));
		themed.add(textPanel("expanded").setExpanded(true));

		Label count = new Label("toggles", () -> toggles);
		count.setOutputMarkupId(true);
		themed.add(count);
		themed.add(new CollapsiblePanel("remembered", new ResourceModel("remembered.title"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Component newBody(String id)
			{
				Fragment body = new Fragment(id, "formBody", CollapsiblePage.this);
				Form<Void> form = new Form<>("form");
				form.add(new TextField<>("name", new PropertyModel<String>(CollapsiblePage.this,
					"name")));
				form.add(new Label("greeting", () -> name != null ? "Hello " + name + "!" : ""));
				body.add(form);
				return body;
			}

			@Override
			protected void onToggle(AjaxRequestTarget target)
			{
				if (isExpanded())
				{
					toggles++;
				}
				target.add(count);
			}
		}.setRememberExpanded(true));
	}

	private CollapsiblePanel textPanel(String id)
	{
		return new CollapsiblePanel(id, new ResourceModel(id + ".title"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Component newBody(String bodyId)
			{
				return new Label(bodyId, new ResourceModel(id + ".body"));
			}
		};
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
			new CssResourceReference(CollapsiblePage.class, "CollapsiblePage.css")));
	}
}
