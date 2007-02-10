/*
 * $Id: PageMapView.java 4507 2006-02-16 22:51:20Z jonathanlocke $
 * $Revision: 4507 $ $Date: 2006-02-16 23:51:20 +0100 (do, 16 feb 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wicket.Page;
import wicket.PageMap;
import wicket.PageParameters;
import wicket.PageMap.Access;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.session.pagemap.IPageMapEntry;
import wicket.util.collections.ArrayListStack;
import wicket.util.lang.Bytes;
import wicket.util.lang.Objects;

/**
 * A Wicket panel that shows interesting information about a given Wicket
 * pagemap.
 * 
 * @author Jonathan Locke
 */
public final class PageMapView extends Panel
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
	public PageMapView(final String id, final PageMap pageMap)
	{
		super(id);

		// Basic attributes
		add(new Label("name", pageMap.getName() == null ? "null" : pageMap.getName()));
		add(new Label("size", "" + Bytes.bytes(pageMap.getSizeInBytes())));

		// Get entry accesses 
		final ArrayListStack accessStack = pageMap.getAccessStack();
		final List reversedAccessStack = new ArrayList();
		reversedAccessStack.addAll(accessStack);
		Collections.reverse(reversedAccessStack);

		// Create the table containing the list the components
		add(new ListView("accesses", reversedAccessStack)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * Populate the table with Wicket elements
			 */
			protected void populateItem(final ListItem listItem)
			{
				final Access access = (Access)listItem.getModelObject();
				IPageMapEntry entry = pageMap.getEntry(access.getId());
				PageParameters parameters = new PageParameters();
				parameters.put("pageId", "" + entry.getNumericId());
				Link link = new BookmarkablePageLink("link", InspectorPage.class, parameters);
				link.add(new Label("id", "" + entry.getNumericId()));
				listItem.add(link);
				listItem.add(new Label("class", "" + entry.getClass().getName()));
				long size;
				int versions;
				if (entry instanceof Page)
				{
					Page page = (Page)entry;
					page.detachModels();
					size = page.getSizeInBytes();
					versions = page.getVersions();
				}
				else
				{
					size = Objects.sizeof(entry);
					versions = 0;
				}
				listItem.add(new Label("access", "" + (accessStack.size() - listItem.getIndex())));
				listItem.add(new Label("version", "" + access.getVersion()));
				listItem.add(new Label("versions", "" + versions));
				listItem.add(new Label("size", size == -1 ? "[Unknown]" : "" + Bytes.bytes(size)));
			}
		});
	}
}