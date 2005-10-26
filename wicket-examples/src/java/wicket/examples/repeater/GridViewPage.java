package wicket.examples.repeater;

import wicket.extensions.markup.html.repeater.data.GridView;
import wicket.extensions.markup.html.repeater.data.IDataProvider;
import wicket.extensions.markup.html.repeater.pageable.Item;
import wicket.markup.html.basic.Label;
import wicket.markup.html.navigation.paging.PagingNavigator;

/**
 * page for demonstrating the gridview componnet
 * 
 * @see wicket.extensions.markup.html.repeater.data.GridView
 * @author igor
 * 
 */
public class GridViewPage extends BasePage
{

	/**
	 * Constructor
	 */
	public GridViewPage()
	{
		IDataProvider dataProvider = new ContactDataProvider();
		GridView dv = new GridView("rows", dataProvider)
		{

			protected void populateItem(Item item)
			{
				final Contact contact = (Contact)item.getModelObject();
				item.add(new Label("firstName", contact.getFirstName() + " "
						+ contact.getLastName()));
			}

			protected void populateEmptyItem(Item item)
			{
				item.add(new Label("firstName", "*empty*"));
			}
		};

		dv.setRows(4);
		dv.setColumns(3);

		add(dv);

		add(new PagingNavigator("navigator", dv));

	}
}
