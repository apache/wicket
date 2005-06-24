/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.extensions.markup.html.navmenu;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.extensions.markup.html.resources.StyleSheetReference;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * One row of a menu. Starts by 0 (zero).
 *
 * @author Eelco Hillenius
 */
public final class MenuRow extends Panel
{
	/** this row's style. */
	private final MenuRowStyle style;

	/**
	 * Construct.
	 * @param id component id
	 * @param model row model
	 * @param style row style
	 */
	public MenuRow(final String id, final MenuRowModel model, final MenuRowStyle style)
	{
		super(id, model);
		this.style = style;
		WebMarkupContainer rowContainer = new WebMarkupContainer("rowContainer");
		rowContainer.add(new AttributeModifier("class", true, new Model()
		{
			public Object getObject(Component component)
			{
				return style.getCssClass();
			}
		}));
		rowContainer.add(new RowListView("columns", model));
		add(rowContainer);

		addToHeader(new StyleSheetReference("cssStyleResource", style.getStyleSheetResource()));
	}

	/**
	 * @see wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		return false;
	}

	/**
	 * Listview for a menu row.
	 */
	private final class RowListView extends ListView
	{
		/**
		 * Construct.
		 * @param id
		 * @param model
		 */
		public RowListView(String id, IModel model)
		{
			super(id, model);
			setOptimizeItemRemoval(false);
		}

		/**
		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
		 */
		protected void populateItem(ListItem item)
		{
			final MenuItem menuItem = (MenuItem)item.getModelObject();
			final String label = menuItem.getLabel();
			final BookmarkablePageLink pageLink = new BookmarkablePageLink("link", menuItem
					.getPageClass(), menuItem.getPageParameters());
			pageLink.setAutoEnable(false);
			pageLink.add(new Label("label", label));
			item.add(pageLink);
			item.add(new AttributeModifier("class", true, new Model()
			{
				public Object getObject(final Component component)
				{
					return style.getCSSClass(menuItem, MenuRow.this);
				}
			}));
		}
	}
}
