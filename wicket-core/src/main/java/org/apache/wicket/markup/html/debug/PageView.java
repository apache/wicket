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
package org.apache.wicket.markup.html.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;


/**
 * This is a simple Wicket component that displays all components of a Page in a table
 * representation. Useful for debugging.
 * <p>
 * Simply add this code to your page's constructor:
 * 
 * <pre>
 * add(new PageView(&quot;componentTree&quot;, this));
 * </pre>
 * 
 * And this to your markup:
 * 
 * <pre>
 *  &lt;span wicket:id=&quot;componentTree&quot;/&gt;
 * </pre>
 * 
 * @author Juergen Donnerstag
 */
public final class PageView extends Panel
{
	/**
	 * A meta data key used by RenderPerformaceListener in wicket-devutils to collect the time
	 * needed by a component to render itself
	 */
	public static final MetaDataKey<Long> RENDER_KEY = new MetaDataKey<Long>()
	{
	};

	/**
	 * El cheapo data holder.
	 * 
	 * @author Juergen Donnerstag
	 */
	private static class ComponentData implements IClusterable
	{
		private static final long serialVersionUID = 1L;

		/** Component path. */
		public final String path;

		/** Component type. */
		public final String type;

		/** Component value. */
		public String value;

		/** Size of component in bytes */
		public final long size;

		/** the time it took to rended the component */
		private Long renderDuration;

		ComponentData(String path, String type, long size)
		{
			this.path = path;
			this.type = type;
			this.size = size;
		}
	}

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param page
	 *            The page to be analyzed
	 * @see Component#Component(String)
	 */
	public PageView(final String id, final Page page)
	{
		super(id);

		// Name of page
		add(new Label("info", page == null ? "[Stateless Page]" : page.toString()));

		// Create an empty list. It'll be filled later
		List<ComponentData> data = null;

		String pageRenderDuration = "n/a";

		if (page != null)
		{
			Long renderTime = page.getMetaData(RENDER_KEY);
			if (renderTime != null)
			{
				pageRenderDuration = renderTime.toString();
			}

			// Get the components data and fill and sort the list
			data = new ArrayList<ComponentData>(getComponentData(page));
			Collections.sort(data, new Comparator<ComponentData>()
			{
				@Override
				public int compare(ComponentData o1, ComponentData o2)
				{
					return (o1).path.compareTo((o2).path);
				}
			});
		}
		else
		{
			data = Collections.emptyList();
		}

		add(new Label("pageRenderDuration", pageRenderDuration));


		// Create the table containing the list the components
		add(new ListView<ComponentData>("components", data)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * Populate the table with Wicket elements
			 */
			@Override
			protected void populateItem(final ListItem<ComponentData> listItem)
			{
				final ComponentData componentData = listItem.getModelObject();

				listItem.add(new Label("row", Long.toString(listItem.getIndex() + 1)));
				listItem.add(new Label("path", componentData.path));
				listItem.add(new Label("size", Bytes.bytes(componentData.size).toString()));
				listItem.add(new Label("type", componentData.type));
				listItem.add(new Label("model", componentData.value));
				listItem.add(new Label("renderDuration", componentData.renderDuration != null
					? componentData.renderDuration.toString() : "n/a"));
			}
		});
	}

	/**
	 * Get recursively all components of the page, extract the information relevant for us and add
	 * them to a list.
	 * 
	 * @param page
	 * @return List of component data objects
	 */
	private List<ComponentData> getComponentData(final Page page)
	{
		final List<ComponentData> data = new ArrayList<ComponentData>();

		page.visitChildren(new IVisitor<Component, Void>()
		{
			@Override
			public void component(final Component component, final IVisit<Void> visit)
			{
				if (!component.getPath().startsWith(PageView.this.getPath()))
				{
					final ComponentData componentData;

					// anonymous class? Get the parent's class name
					String name = component.getClass().getName();
					if (name.indexOf("$") > 0)
					{
						name = component.getClass().getSuperclass().getName();
					}

					// remove the path component
					name = Strings.lastPathComponent(name, Component.PATH_SEPARATOR);

					componentData = new ComponentData(component.getPageRelativePath(), name,
						component.getSizeInBytes());

					Long renderDuration = component.getMetaData(RENDER_KEY);
					if (renderDuration != null)
					{
						componentData.renderDuration = renderDuration;
					}

					try
					{
						componentData.value = component.getDefaultModelObjectAsString();
					}
					catch (Exception e)
					{
						componentData.value = e.getMessage();
					}

					data.add(componentData);
				}

			}
		});

		return data;
	}
}