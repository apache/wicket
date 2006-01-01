/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.markup.html.debug;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import wicket.IPageMapEntry;
import wicket.Page;
import wicket.PageMap;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.util.lang.Bytes;
import wicket.util.profile.ObjectProfiler;

/**
 * A Wicket panel that shows interesting information about a given Wicket
 * pagemap.
 * 
 * @author Jonathan Locke
 */
public final class WicketPageMapView extends Panel
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
	public WicketPageMapView(final String id, final PageMap pageMap)
	{
		super(id);

		// Basic attributes
		add(new Label("name", pageMap.getName() == null ? "null" : pageMap.getName()));

		// Get pagemaps
		final List entries = pageMap.getEntries();

		// Sort on access
		Collections.sort(entries, new Comparator()
		{
			public int compare(Object a, Object b)
			{
				IPageMapEntry ea = (IPageMapEntry)a;
				IPageMapEntry eb = (IPageMapEntry)b;
				return eb.getAccessSequenceNumber() - ea.getAccessSequenceNumber();
			}
		});

		// Create the table containing the list the components
		add(new ListView("entries", entries)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * Populate the table with Wicket elements
			 */
			protected void populateItem(final ListItem listItem)
			{
				IPageMapEntry entry = (IPageMapEntry)listItem.getModelObject();
				listItem.add(new Label("id", "" + entry.getNumericId()));
				listItem.add(new Label("class", "" + entry.getClass().getName()));
				int size;
				int versions;
				if (entry instanceof Page)
				{
					size = ((Page)entry).getSize();
					versions = ((Page)entry).getCurrentVersionNumber() + 1;
				}
				else
				{
					size = ObjectProfiler.sizeof(entry);
					versions = 1;
				}
				listItem.add(new Label("versions", "" + versions));
				listItem.add(new Label("size", (entry instanceof WicketInspector)
						? "(inspector)"
						: "" + Bytes.bytes(size)));
			}
		});
	}
}