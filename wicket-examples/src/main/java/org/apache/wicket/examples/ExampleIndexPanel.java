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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;

/**
 * Lists the examples of a package, each as a link to it followed by its description.
 * <p>
 * The list is not written down anywhere: it is whatever {@link ExamplePages} finds next to the
 * index page, so a new example is added by adding it - the index picks it up. What the index shows
 * of an example is what the example's own bundle says, as described on {@link WicketExamplePage}:
 * <ul>
 * <li>{@code title} - the link text. An example without one is left out, on the assumption that it
 * is a page the example uses rather than an example of its own.</li>
 * <li>{@code description} - shown after the link, optional.</li>
 * <li>{@code order} - a number, optional. Examples that carry one come first, in that order; the
 * rest follow alphabetically by title. Only worth setting where the examples build on each other
 * and should be read in sequence.</li>
 * <li>{@code index=false} - for an example that needs a title of its own but is not one to start
 * at, such as the second page of an example that spans several.</li>
 * <li>{@code group} - which group of the index the example belongs to, optional.</li>
 * </ul>
 * An index that groups its examples lists the groups in its own bundle, in the order they are to
 * be shown, and gives each a heading:
 *
 * <pre>
 * groups=output,layout,links,forms
 * group.output=Output
 * </pre>
 *
 * Examples naming no group, or one the index does not list, come last under no heading. An index
 * that says nothing about groups shows a single ungrouped list, which is the usual case.
 *
 * @see ExamplePages
 */
public class ExampleIndexPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	private final Class<? extends WicketExamplePage> index;

	private final Class<?> type;

	private final boolean recursive;

	/**
	 * Lists the example pages of the index page's own package.
	 *
	 * @param id
	 *            component id
	 * @param index
	 *            the index page this panel is shown on
	 */
	public ExampleIndexPanel(final String id, final Class<? extends WicketExamplePage> index)
	{
		this(id, index, false);
	}

	/**
	 * @param id
	 *            component id
	 * @param index
	 *            the index page this panel is shown on
	 * @param recursive
	 *            whether to list the examples of sub packages too
	 */
	public ExampleIndexPanel(final String id, final Class<? extends WicketExamplePage> index,
		final boolean recursive)
	{
		this(id, index, WicketExamplePage.class, recursive);
	}

	/**
	 * @param id
	 *            component id
	 * @param index
	 *            the index page this panel is shown on
	 * @param type
	 *            the type an example has to be of, for an index over something other than pages
	 * @param recursive
	 *            whether to list the examples of sub packages too
	 */
	public ExampleIndexPanel(final String id, final Class<? extends WicketExamplePage> index,
		final Class<?> type, final boolean recursive)
	{
		super(id);

		this.index = index;
		this.type = type;
		this.recursive = recursive;
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		IModel<List<Group>> groups = this::groups;

		add(new ListView<>("groups", groups)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<Group> group)
			{
				String heading = group.getModelObject().heading();

				group.add(new Label("heading", heading)
					.setVisible(Strings.isEmpty(heading) == false));
				group.add(new ListView<Class<?>>("examples", group.getModelObject().examples())
				{
					private static final long serialVersionUID = 1L;

					@Override
					protected void populateItem(final ListItem<Class<?>> item)
					{
						populateExample(item);
					}
				});
			}
		});
	}

	/**
	 * @param id
	 *            component id
	 * @param example
	 *            an example found next to the index
	 * @return the link to it, a {@link BookmarkablePageLink} by default
	 */
	protected AbstractLink link(final String id, final Class<?> example)
	{
		return new BookmarkablePageLink<>(id, example.asSubclass(Page.class));
	}

	/**
	 * @param example
	 *            an example found next to the index
	 * @return whether to list it, {@code true} by default
	 */
	protected boolean include(final Class<?> example)
	{
		return true;
	}

	private void populateExample(final ListItem<Class<?>> item)
	{
		Class<?> example = item.getModelObject();

		AbstractLink link = link("link", example);
		link.add(new Label("title", title(example)));
		item.add(link);

		String description = string(example, "description");
		WebMarkupContainer describe = new WebMarkupContainer("describe");
		describe.setVisible(Strings.isEmpty(description) == false);
		describe.add(new Label("description", description));
		item.add(describe);
	}

	private List<Group> groups()
	{
		List<Class<?>> examples = examples();
		List<String> declared = declaredGroups();

		List<Group> groups = new ArrayList<>();
		for (String name : declared)
		{
			add(groups, WicketExamplePage.string(index, "group." + name, this),
				examples.stream().filter(example -> name.equals(group(example))).toList());
		}
		add(groups, null, examples.stream()
			.filter(example -> declared.contains(group(example)) == false)
			.toList());
		return groups;
	}

	private static void add(final List<Group> groups, final String heading,
		final List<Class<?>> examples)
	{
		if (examples.isEmpty() == false)
		{
			groups.add(new Group(heading, examples));
		}
	}

	private List<String> declaredGroups()
	{
		String groups = WicketExamplePage.string(index, "groups", this);
		return Strings.isEmpty(groups) ? List.of()
			: Arrays.stream(groups.split(",")).map(String::trim).toList();
	}

	private List<Class<?>> examples()
	{
		List<Class<?>> found = ExamplePages.inPackage(index.getPackageName(), type, recursive);

		Set<Class<?>> extended = found.stream()
			.map(Class::getSuperclass)
			.collect(Collectors.toSet());

		Comparator<Class<?>> order = Comparator.<Class<?>> comparingInt(this::order)
			.thenComparing(this::title);

		return found.stream()
			.filter(example -> example != index)
			.filter(example -> extended.contains(example) == false)
			.filter(example -> Strings.isEmpty(title(example)) == false)
			.filter(example -> "false".equals(string(example, "index")) == false)
			.filter(this::include)
			.sorted(order)
			.toList();
	}

	private int order(final Class<?> example)
	{
		String order = string(example, "order");
		return Strings.isEmpty(order) ? Integer.MAX_VALUE : Integer.parseInt(order.trim());
	}

	private String group(final Class<?> example)
	{
		String group = string(example, "group");
		return group == null ? "" : group.trim();
	}

	private String title(final Class<?> example)
	{
		return string(example, "title");
	}

	private String string(final Class<?> example, final String key)
	{
		return WicketExamplePage.string(example, key, this);
	}

	private record Group(String heading, List<Class<?>> examples) implements Serializable
	{
	}
}
