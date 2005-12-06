package wicket.extensions.markup.html.repeater.data.table;

import wicket.AttributeModifier;
import wicket.extensions.markup.html.repeater.data.DataView;
import wicket.extensions.markup.html.repeater.data.grid.AbstractDataGridView;
import wicket.extensions.markup.html.repeater.data.grid.DataGridView;
import wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.navigation.paging.PagingNavigator;
import wicket.model.Model;

public class NavigationToolbar extends Toolbar
{
	private static final long serialVersionUID = 1L;

	public NavigationToolbar(final AbstractDataTable table)
	{
		super(AbstractDataTable.TOOLBAR_COMPONENT_ID);
		WebMarkupContainer span=new WebMarkupContainer("span");
		add(span);
		span.add(new AttributeModifier("colspan", true, new Model(String.valueOf(table.getColumns().length))));
		
		span.add(newPagingNavigator("navigator", table));
		span.add(newNavigatorLabel("navigatorLabel", table));
	}
	
	/**
	 * Factory method used to create the paging navigator that will be used by
	 * the datatable
	 * 
	 * @param navigatorId
	 *            component id the navigator should be created with
	 * @param dataView
	 *            dataview used by datatable
	 * @return paging navigator that will be used by the datatable
	 */
	protected PagingNavigator newPagingNavigator(String navigatorId, final AbstractDataTable dataView)
	{
		return new PagingNavigator(navigatorId, dataView)
		{
			private static final long serialVersionUID = 1L;

			public boolean isVisible()
			{
				return dataView.getItemCount() > 0;
			}
		};
	}

	/**
	 * Factory method used to create the navigator label that will be used by
	 * the datatable
	 * 
	 * @param navigatorId
	 *            component id navigator label should be created with
	 * @param dataView
	 *            dataview used by datatable
	 * @return navigator label that will be used by the datatable
	 * 
	 */
	protected WebComponent newNavigatorLabel(String navigatorId, final AbstractDataTable dataView)
	{
		return new NavigatorLabel(navigatorId, dataView);
	}

}
