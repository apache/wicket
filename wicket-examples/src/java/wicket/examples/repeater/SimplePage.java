package wicket.examples.repeater;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.extensions.markup.html.repeater.data.DataView;
import wicket.extensions.markup.html.repeater.pageable.Item;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;

/**
 * Page that demonstrates a simple dataview.
 * 
 * @see wicket.extensions.markup.html.repeater.data.DataView
 * 
 * @author igor
 * 
 */
public class SimplePage extends BasePage
{
	/**
	 * constructor
	 */
	public SimplePage()
	{

		add(new DataView("simple", new ContactDataProvider())
		{

			protected void populateItem(final Item item)
			{
				Contact contact = (Contact)item.getModelObject();
				item.add(new ActionPanel("actions", item.getModel()));
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

		});
	}
}
