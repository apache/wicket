/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.Component;
import wicket.extensions.markup.html.repeater.RepeatingView;
import wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import wicket.extensions.markup.html.repeater.data.table.DataTable;
import wicket.extensions.markup.html.repeater.data.table.IColumn;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.util.string.AppendingStringBuffer;

/**
 * Toolbar that creates a form to hold form components used to filter data in
 * the data table. Form components are provided by columns that implement
 * IFilteredColumn.
 * 
 * @author Igor Vaynberg (ivaynber)
 * 
 */
public class FilterToolbar extends AbstractToolbar
{
	private static final long serialVersionUID = 1L;
	private static final String FILTER_COMPONENT_ID = "filter";

	/**
	 * Constructor
	 * 
	 * @param table
	 *            data table this toolbar will be added to
	 * @param stateLocator
	 *            locator responsible for finding object used to store filter's
	 *            state
	 */
	public FilterToolbar(final DataTable table, final IFilterStateLocator stateLocator)
	{
		super(table);

		if (table == null)
		{
			throw new IllegalArgumentException("argument [table] cannot be null");
		}
		if (stateLocator == null)
		{
			throw new IllegalArgumentException("argument [stateLocator] cannot be null");
		}

		// create the form used to contain all filter components

		final FilterForm form = new FilterForm("filter-form", stateLocator)
		{
			private static final long serialVersionUID = 1L;

			protected void onSubmit()
			{
				table.setCurrentPage(0);
			}
		};
		add(form);

		// add javascript to restore focus to a filter component

		add(new WebMarkupContainer("focus-restore")
		{
			private static final long serialVersionUID = 1L;

			protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
			{
				AppendingStringBuffer script = new AppendingStringBuffer("<script>_filter_focus_restore('").append(
						form.getFocusTrackerFieldCssId()).append("');</script>");
				replaceComponentTagBody(markupStream, openTag, script);
			}
		});

		// populate the toolbar with components provided by filtered columns

		RepeatingView filters = new RepeatingView("filters");
		form.add(filters);

		IColumn[] cols = table.getColumns();
		for (int i = 0; i < cols.length; i++)
		{
			WebMarkupContainer item = new WebMarkupContainer(filters.newChildId());
			item.setRenderBodyOnly(true);

			IColumn col = cols[i];
			Component filter = null;

			if (col instanceof IFilteredColumn)
			{
				IFilteredColumn filteredCol = (IFilteredColumn)col;
				filter = filteredCol.getFilter(FILTER_COMPONENT_ID, form);
			}

			if (filter == null)
			{
				filter = new NoFilter(FILTER_COMPONENT_ID);
			}
			else
			{
				if (!filter.getId().equals(FILTER_COMPONENT_ID))
				{
					throw new IllegalStateException(
							"filter component returned  with an invalid component id. invalid component id ["
									+ filter.getId() + "] required component id ["
									+ FILTER_COMPONENT_ID + "] generating column ["
									+ col.toString() + "] ");
				}
			}

			item.add(filter);

			filters.add(item);
		}

	}

}
