/*
 * $Id: PageMapView.java 4507 2006-02-16 22:51:20Z jonathanlocke $ $Revision:
 * 4507 $ $Date: 2006-02-16 23:51:20 +0100 (do, 16 feb 2006) $
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

import wicket.AccessStackPageMap;
import wicket.IPageMap;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.PageParameters;
import wicket.AccessStackPageMap.Access;
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
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            The component id
	 * @param pageMap
	 *            Page map to show
	 */
	public PageMapView(MarkupContainer parent, final String id, final IPageMap pageMap)
	{
		super(parent, id);

		// Basic attributes
		new Label(this, "name", pageMap.getName() == null ? "null" : pageMap.getName());
		new Label(this, "size", "" + Bytes.bytes(pageMap.getSizeInBytes()));

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
		new ListView<Access>(this, "accesses", reversedAccessStack)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * Populate the table with Wicket elements
			 */
			@Override
			protected void populateItem(final ListItem listItem)
			{
				final Access access = (Access)listItem.getModelObject();
				IPageMapEntry entry = pageMap.getEntry(access.getId());
				PageParameters parameters = new PageParameters();
				parameters.put("pageId", "" + entry.getNumericId());
				Link link = new BookmarkablePageLink(listItem, "link", InspectorPage.class,
						parameters);
				new Label(link, "id", "" + entry.getNumericId());
				new Label(listItem, "class", "" + entry.getClass().getName());
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
				new Label(listItem, "access", "" + (accessStack.size() - listItem.getIndex()));
				new Label(listItem, "version", "" + access.getVersion());
				new Label(listItem, "versions", "" + versions);
				new Label(listItem, "size", size == -1 ? "[Unknown]" : "" + Bytes.bytes(size));
			}
		};
	}
}