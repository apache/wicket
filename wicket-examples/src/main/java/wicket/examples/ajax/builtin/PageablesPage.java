package wicket.examples.ajax.builtin;

import java.util.Arrays;

import wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.PageableListView;

/**
 * Shows an ajaxian paging navigator in action.
 * 
 * @author Martijn Dashorst
 */
public class PageablesPage extends BasePage
{
	private static final String[] names = { "Doe, John", "Presley, Elvis", "Presly, Priscilla",
			"John, Elton", "Jackson, Michael", "Bush, George", "Baker, George",
			"Stallone, Sylvester", "Murphy, Eddie", "Potter, Harry", "Balkenende, Jan Peter",
			"Two Shoes, Goody", "Goodman, John", "Candy, John", "Belushi, James",
			"Jones, James Earl", "Kelly, Grace", "Osborne, Kelly", "Cartman", "Kenny",
			"Schwarzenegger, Arnold", "Pitt, Brad", "Richie, Nicole", "Richards, Denise",
			"Sheen, Charlie", "Sheen, Martin", "Esteves, Emilio", "Baldwin, Alec",
			"Knowles, Beyonce", "Affleck, Ben", "Lavigne, Avril", "Cuthbert, Elisha",
			"Longoria, Eva", "Clinton, Bill", "Willis, Bruce", "Farrell, Colin",
			"Hasselhoff, David", "Moore, Demi", };

	/**
	 * Constructor.
	 */
	public PageablesPage()
	{
		WebMarkupContainer datacontainer = new WebMarkupContainer("data");
		datacontainer.setOutputMarkupId(true);
		add(datacontainer);

		PageableListView listview = new PageableListView("rows", Arrays.asList(names), 10)
		{
			protected void populateItem(ListItem item)
			{
				item.add(new Label("name", item.getModelObjectAsString()));
			}
		};

		datacontainer.add(listview);
		datacontainer.add(new AjaxPagingNavigator("navigator", listview));
	}
}
