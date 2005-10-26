package wicket.examples.repeater;

import java.util.ArrayList;
import java.util.List;

import wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import wicket.extensions.markup.html.repeater.data.table.DataTable;
import wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import wicket.markup.html.list.ListItem;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * demo page for the datatable component
 * 
 * @see wicket.extensions.markup.html.repeater.data.table.DataTable
 * @author igor
 * 
 */
public class DataTablePage extends BasePage
{
	/**
	 * constructor
	 */
	public DataTablePage()
	{

		List columns = new ArrayList();

		columns.add(new AbstractColumn(new Model("Actions"))
		{

			public void populateItem(ListItem cellItem, String componentId, IModel model)
			{
				cellItem.add(new ActionPanel(componentId, model));
			}

		});
		columns.add(new PropertyColumn(new Model("ID"), "id"));
		columns.add(new PropertyColumn(new Model("First Name"), "firstName", "firstName"));
		columns.add(new PropertyColumn(new Model("Last Name"), "lastName", "lastName"));
		columns.add(new PropertyColumn(new Model("Home Phone"), "homePhone"));
		columns.add(new PropertyColumn(new Model("Cell Phone"), "cellPhone"));

		add(new DataTable("table", columns, new SortableContactDataProvider(), 8));


	}
}
