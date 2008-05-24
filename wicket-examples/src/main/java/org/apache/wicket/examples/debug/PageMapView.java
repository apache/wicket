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
package org.apache.wicket.examples.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.AccessStackPageMap;
import org.apache.wicket.IPageMap;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.AccessStackPageMap.Access;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.session.pagemap.IPageMapEntry;
import org.apache.wicket.util.collections.ArrayListStack;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.lang.Objects;


/**
 * A Wicket panel that shows interesting information about a given Wicket pagemap.
 * 
 * @author Jonathan Locke
 */
public final class PageMapView extends Panel<Void>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            The component id
	 * @param pageMap
	 *            Page map to show
	 */
	public PageMapView(final String id, final IPageMap pageMap)
	{
		super(id);

		// Basic attributes
		add(new Label<String>("name", pageMap.getName() == null ? "null" : pageMap.getName()));
		add(new Label<String>("size", "" + Bytes.bytes(pageMap.getSizeInBytes())));

		// Get entry accesses
		// Get entry accesses
		final ArrayListStack<Access> accessStack;
		if (pageMap instanceof AccessStackPageMap)
		{
			accessStack = ((AccessStackPageMap)pageMap).getAccessStack();
		}
		else
		{
			accessStack = new ArrayListStack<Access>();
		}
		final List<Access> reversedAccessStack = new ArrayList<Access>();
		reversedAccessStack.addAll(accessStack);
		Collections.reverse(reversedAccessStack);

		// Create the table containing the list the components
		add(new ListView<Access>("accesses", reversedAccessStack)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * Populate the table with Wicket elements
			 */
			@Override
			protected void populateItem(final ListItem<Access> listItem)
			{
				final Access access = listItem.getModelObject();
				IPageMapEntry entry = pageMap.getEntry(access.getId());
				PageParameters parameters = new PageParameters();
				parameters.put("pageId", "" + entry.getNumericId());
				Link<?> link = new BookmarkablePageLink("link", InspectorPage.class, parameters);
				link.add(new Label<String>("id", "" + entry.getNumericId()));
				listItem.add(link);
				listItem.add(new Label<String>("class", "" + entry.getClass().getName()));
				long size;
				int versions;
				if (entry instanceof Page)
				{
					Page<?> page = (Page<?>)entry;
					page.detachModels();
					size = page.getSizeInBytes();
					versions = page.getVersions();
				}
				else
				{
					size = Objects.sizeof(entry);
					versions = 0;
				}
				listItem.add(new Label<String>("access", "" +
					(accessStack.size() - listItem.getIndex())));
				listItem.add(new Label<String>("version", "" + access.getVersion()));
				listItem.add(new Label<String>("versions", "" + versions));
				listItem.add(new Label<String>("size", size == -1 ? "[Unknown]" : "" +
					Bytes.bytes(size)));
			}
		});
	}
}