package wicket.extensions.markup.html.repeater.data.table;

import wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;

/**
 * Toolbars that displays column headers. If the column is sortable a sortable
 * header will be displayed.
 * 
 * @author igor
 * 
 */
public class HeadersToolbar extends Toolbar
{
	private static final long serialVersionUID = 1L;

	public HeadersToolbar(final AbstractDataTable table,
			final ISortStateLocator stateLocator)
	{
		super(AbstractDataTable.TOOLBAR_COMPONENT_ID);
		add(new ListView("headers", table.getColumns())
		{
			private static final long serialVersionUID = 1L;

			protected void populateItem(ListItem item)
			{
				final IColumn column = (IColumn)item.getModelObject();
				WebMarkupContainer header = null;
				if (column.isSortable())
				{
					header = new OrderByBorder("header", column.getSortProperty(), stateLocator)
					{

						private static final long serialVersionUID = 1L;

						protected void onSortChanged()
						{
							table.setCurrentPage(0);
						}
					};

				}
				else
				{
					header = new WebMarkupContainer("header");
				}
				item.add(header);
				header.add(column.getHeader("label"));
			}

		});
	}

}
