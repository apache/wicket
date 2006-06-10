/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 00:46:21 +0200 (vr, 26 mei 2006) $
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
package wicket.extensions.markup.html.repeater.data.table;

import wicket.AttributeModifier;
import wicket.MarkupContainer;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.navigation.paging.PagingNavigator;
import wicket.model.Model;

/**
 * Toolbar that displays links used to navigate the pages of the datatable as
 * well as a message about which rows are being displayed and their total number
 * in the data table.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class NavigationToolbar extends AbstractToolbar
{
	private static final long serialVersionUID = 1L;

	private DataTable table;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent component
	 * @param id
	 *            component id
	 * 
	 * @param table
	 *            data table this toolbar will be attached to
	 */
	public NavigationToolbar(MarkupContainer parent, String id, final DataTable table)
	{
		super(parent, id, table);
		this.table = table;

		WebMarkupContainer span = new WebMarkupContainer(this, "span");
		span.add(new AttributeModifier("colspan", true, new Model<String>(String.valueOf(table
				.getColumns().length))));

		newPagingNavigator(span, "navigator", table);
		newNavigatorLabel(span, "navigatorLabel", table);
	}


	/**
	 * Factory method used to create the paging navigator that will be used by
	 * the datatable
	 * 
	 * @param parent
	 * 
	 * @param navigatorId
	 *            component id the navigator should be created with
	 * @param table
	 *            dataview used by datatable
	 * @return paging navigator that will be used to navigate the data table
	 */
	protected PagingNavigator newPagingNavigator(MarkupContainer parent, String navigatorId,
			final DataTable table)
	{
		return new PagingNavigator(parent, navigatorId, table);
	}

	/**
	 * Factory method used to create the navigator label that will be used by
	 * the datatable
	 * 
	 * @param parent
	 * 
	 * @param navigatorId
	 *            component id navigator label should be created with
	 * @param table
	 *            dataview used by datatable
	 * @return navigator label that will be used to navigate the data table
	 * 
	 */
	protected WebComponent newNavigatorLabel(MarkupContainer parent, String navigatorId,
			final DataTable table)
	{
		return new NavigatorLabel(parent, navigatorId, table);
	}

	/**
	 * Hides this toolbar when no rows are visible or number of rows is set to
	 * Integer.MAX_VALUE
	 * 
	 * @see wicket.Component#isVisible()
	 */
	@Override
	public boolean isVisible()
	{
		return table.getRowCount() > 0 && table.getRowsPerPage() < Integer.MAX_VALUE;
	}
}
