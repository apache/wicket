package wicket.extensions.markup.html.repeater.data.table;

import wicket.Component;
import wicket.extensions.markup.html.repeater.OrderedRepeatingView;
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

		OrderedRepeatingView headers=new OrderedRepeatingView("headers");
		add(headers);
		IColumn[] cols=table.getColumns();
		
		for (int i=0;i<cols.length;i++) {
			WebMarkupContainer item=new WebMarkupContainer(headers.newChildId());
			headers.add(item);
			
			IColumn column=cols[i];
			WebMarkupContainer header=null;
			if (column.isSortable())
			{
				header = new OrderByBorder("header", column.getSortProperty(), stateLocator)
				{

					private static final long serialVersionUID = 1L;

					protected void onSortChanged()
					{
						//TODO this is a bit nasty, add setcurrentpage to abstract table?
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
			
	}

}
