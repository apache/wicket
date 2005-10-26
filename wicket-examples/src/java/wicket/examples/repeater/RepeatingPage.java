package wicket.examples.repeater;

import java.util.Iterator;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.extensions.markup.html.repeater.RepeatingView;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;

/**
 * page that demonstrates a simple repeater view.
 * 
 * @see wicket.extensions.markup.html.repeater.OrderedRepeatingView
 * 
 * @author igor
 * 
 */
public class RepeatingPage extends BasePage
{
	/**
	 * Constructor
	 */
	public RepeatingPage()
	{

		Iterator contacts = new ContactDataProvider().iterator(0, 10);

		RepeatingView repeating = new RepeatingView("repeating");
		add(repeating);


		int index = 0;
		while (contacts.hasNext())
		{
			WebMarkupContainer item = new WebMarkupContainer(repeating.newChildId());
			repeating.add(item);
			Contact contact = (Contact)contacts.next();

			item.add(new ActionPanel("actions", new DetachableContactModel(contact)));
			item.add(new Label("contactid", String.valueOf(contact.getId())));
			item.add(new Label("firstname", contact.getFirstName()));
			item.add(new Label("lastname", contact.getLastName()));
			item.add(new Label("homephone", contact.getHomePhone()));
			item.add(new Label("cellphone", contact.getCellPhone()));

			final int idx = index;
			item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel()
			{

				public Object getObject(Component component)
				{
					return (idx % 2 == 1) ? "even" : "odd";
				}

			}));

			index++;

		}

	}
}
