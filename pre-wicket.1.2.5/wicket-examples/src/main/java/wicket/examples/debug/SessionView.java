/*
 * $Id: SessionView.java 4768 2006-03-06 01:05:00Z joco01 $
 * $Revision: 4768 $ $Date: 2006-03-06 02:05:00 +0100 (ma, 06 mrt 2006) $
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

import java.util.List;

import wicket.Component;
import wicket.PageMap;
import wicket.Session;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;
import wicket.util.lang.Bytes;
import wicket.util.lang.Objects;

/**
 * A Wicket panel that shows interesting information about a given Wicket
 * session.
 * 
 * @author Jonathan Locke
 */
public final class SessionView extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @see Component#Component(String)
	 */
	public SessionView(final String id, final Session session)
	{
		super(id);

		// Basic attributes
		add(new Label("id", session.getId()));
		add(new Label("locale", session.getLocale().toString()));
		add(new Label("style", session.getStyle() == null ? "[None]" : session.getStyle()));
		add(new Label("size", new Model()
		{
			private static final long serialVersionUID = 1L;

			public Object getObject(Component component) 
			{
				return Bytes.bytes(Objects.sizeof(session));
			}
		}));
		add(new Label("totalSize", new Model()
		{
			private static final long serialVersionUID = 1L;

			public Object getObject(Component component) 
			{
				return Bytes.bytes(session.getSizeInBytes());
			}
		}));

		// Get pagemaps
		final List pagemaps = session.getPageMaps();

		// Create the table containing the list the components
		add(new ListView("pagemaps", pagemaps)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * Populate the table with Wicket elements
			 */
			protected void populateItem(final ListItem listItem)
			{
				PageMap p = (PageMap)listItem.getModelObject();
				listItem.add(new PageMapView("pagemap", p));
			}
		});
	}
}
