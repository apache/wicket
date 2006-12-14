package wicket.examples.repeater;

import java.util.ArrayList;
import java.util.List;

import wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import wicket.markup.repeater.Item;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * @author Martijn Dashorst
 */
public class AjaxDataTablePage extends BasePage
{
	/**
	 * Constructor.
	 */
	public AjaxDataTablePage() {
		List columns = new ArrayList();

		columns.add(new AbstractColumn(new Model("Actions"))
		{
			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				cellItem.add(new ActionPanel(componentId, model));
			}
		});

		columns.add(new PropertyColumn(new Model("ID"), "id"));
		columns.add(new PropertyColumn(new Model("First Name"), "firstName", "firstName"));
		columns.add(new PropertyColumn(new Model("Last Name"), "lastName", "lastName"));
		columns.add(new PropertyColumn(new Model("Home Phone"), "homePhone"));
		columns.add(new PropertyColumn(new Model("Cell Phone"), "cellPhone"));

		add(new AjaxFallbackDefaultDataTable("table", columns, new SortableContactDataProvider(), 8));
	}
}
