package wicket.extensions.ajax.markup.html.repeater.data.table;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import wicket.extensions.markup.html.repeater.data.table.DataTable;
import wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import wicket.markup.html.navigation.paging.PagingNavigator;

/**
 * Toolbar that displays (Ajax) links used to navigate the pages of the
 * datatable as well as a message about which rows are being displayed and their
 * total number in the data table.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst (dashorst)
 * @since 1.2.1
 */
public class AjaxNavigationToolbar extends NavigationToolbar
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param table
	 *            data table this toolbar will be attached to
	 */
	public AjaxNavigationToolbar(final DataTable table)
	{
		super(table);
	}


	/**
	 * Factory method used to create the paging navigator that will be used by
	 * the datatable.
	 * 
	 * @param navigatorId
	 *            component id the navigator should be created with
	 * @param table
	 *            dataview used by datatable
	 * @return paging navigator that will be used to navigate the data table
	 */
	protected PagingNavigator newPagingNavigator(String navigatorId, final DataTable table)
	{
		return new AjaxPagingNavigator(navigatorId, table)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * Implement our own ajax event handling in order to update the
			 * datatable itself, as the default implementation doesn't support
			 * DataViews.
			 * 
			 * @see AjaxPagingNavigator#onAjaxEvent(AjaxRequestTarget)
			 */
			protected void onAjaxEvent(AjaxRequestTarget target)
			{
				target.addComponent(table);
			}
		};
	}
}
