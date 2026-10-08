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
package org.apache.wicket.examples;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.Strings;

/**
 * Lists examples that are applications of their own, for an index that gathers a few of them under
 * one entry of the main menu.
 * <p>
 * {@link ExampleIndexPanel} cannot do this: each of these examples is a separate application with
 * its own filter mapping in {@code web.xml}, so they are reached by path rather than as pages of
 * the application the index belongs to, and their titles are not on this application's classpath
 * to be read. The index page therefore says in its own bundle which paths it gathers and what to
 * call them:
 *
 * <pre>
 * examples=authentication1,authentication2
 * authentication1.title=authentication-1
 * authentication1.description=A very simple authentication example.
 * </pre>
 *
 * @see ExampleIndexPanel
 */
public class ExampleApplicationsPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	private final Class<? extends WicketExamplePage> index;

	/**
	 * @param id
	 *            component id
	 * @param index
	 *            the index page this panel is shown on, whose bundle lists the examples
	 */
	public ExampleApplicationsPanel(final String id,
		final Class<? extends WicketExamplePage> index)
	{
		super(id);

		this.index = index;
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		IModel<List<String>> examples = this::examples;

		add(new ListView<>("examples", examples)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<String> item)
			{
				String path = item.getModelObject();

				ExternalLink link = new ExternalLink("link", RequestCycle.get()
					.getUrlRenderer()
					.renderContextRelativeUrl(path));
				link.add(new Label("title", string(path, "title")));
				item.add(link);

				String description = string(path, "description");
				WebMarkupContainer describe = new WebMarkupContainer("describe");
				describe.setVisible(Strings.isEmpty(description) == false);
				describe.add(new Label("description", description));
				item.add(describe);
			}
		});
	}

	private List<String> examples()
	{
		String examples = WicketExamplePage.string(index, "examples", this);
		return Strings.isEmpty(examples) ? List.of()
			: Arrays.stream(examples.split(",")).map(String::trim).toList();
	}

	private String string(final String path, final String key)
	{
		return WicketExamplePage.string(index, path + '.' + key, this);
	}
}
