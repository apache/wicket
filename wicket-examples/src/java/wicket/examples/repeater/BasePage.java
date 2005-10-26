package wicket.examples.repeater;

import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.PropertyModel;

/**
 * Base page for component demo pages.
 * 
 * @author igor
 * 
 */
public class BasePage extends ExamplePage
{
	/**
	 * Constructor
	 */
	public BasePage()
	{
		add(new Label("selectedLabel", new PropertyModel(this, "selectedContactLabel")));
	}

	class ActionPanel extends Panel
	{
		/**
		 * @param id
		 *            component id
		 * @param model
		 *            model for contact
		 */
		public ActionPanel(String id, IModel model)
		{
			super(id, model);
			add(new Link("select")
			{
				public void onClick()
				{
					BasePage.this.selected = (Contact)getParent().getModelObject();
				}
			});
		}
	}


	private Contact selected;

	/**
	 * @return string representation of selceted contact property
	 */
	public String getSelectedContactLabel()
	{
		if (selected == null)
		{
			return "No Contact Selected";
		}
		else
		{
			return selected.getFirstName() + " " + selected.getLastName();
		}
	}

}
