package wicket.examples.repeater;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.extensions.markup.html.repeater.data.DataView;
import wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import wicket.extensions.markup.html.repeater.pageable.Item;
import wicket.extensions.markup.html.repeater.pageable.ReuseIfModelsEqualStrategy;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;
import wicket.markup.html.navigation.paging.PagingNavigator;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * page that demonstrates dataview with ReuseIfModelsEqualStrategy
 * 
 * @author igor
 * 
 */
public class OIRPage extends BasePage
{
	private static class HighlitableDataItem extends Item
	{
		private boolean highlite = false;

		/**
		 * toggles highlite
		 */
		public void toggleHighlite()
		{
			highlite = !highlite;
		}

		/**
		 * Constructor
		 * 
		 * @param id
		 * @param index
		 * @param model
		 */
		public HighlitableDataItem(String id, int index, IModel model)
		{
			super(id, index, model);
			add(new AttributeModifier("style", true, new Model("background-color:#80b6ed;"))
			{
				public boolean isEnabled()
				{
					return HighlitableDataItem.this.highlite;
				}
			});
		}

	}

	/**
	 * Constructor
	 */
	public OIRPage()
	{
		final DataView dataView = new DataView("oir", new SortableContactDataProvider())
		{

			protected void populateItem(final Item item)
			{
				Contact contact = (Contact)item.getModelObject();
				item.add(new ActionPanel("actions", item.getModel()));
				item.add(new Link("toggleHighlite")
				{

					public void onClick()
					{
						HighlitableDataItem hitem = (HighlitableDataItem)item;
						hitem.toggleHighlite();
					}

				});
				item.add(new Label("contactid", String.valueOf(contact.getId())));
				item.add(new Label("firstname", contact.getFirstName()));
				item.add(new Label("lastname", contact.getLastName()));
				item.add(new Label("homephone", contact.getHomePhone()));
				item.add(new Label("cellphone", contact.getCellPhone()));

				item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel()
				{

					public Object getObject(Component component)
					{
						return (item.getIndex() % 2 == 1) ? "even" : "odd";
					}

				}));
			}

			protected Item newItem(String id, int index, IModel model)
			{
				return new HighlitableDataItem(id, index, model);
			}

		};

		dataView.setItemsPerPage(8);
		dataView.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());

		add(new OrderByBorder("orderByFirstName", "firstName", dataView));

		add(new OrderByBorder("orderByLastName", "lastName", dataView));

		add(dataView);

		add(new PagingNavigator("navigator", dataView));
	}
}
