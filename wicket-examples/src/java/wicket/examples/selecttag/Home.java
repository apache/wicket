package wicket.examples.selecttag;

import wicket.Component;
import wicket.PageParameters;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.model.DetachableChoiceList;
import wicket.markup.html.form.model.IChoice;
import wicket.model.AbstractModel;
import wicket.model.PropertyModel;

/**
 * @author jcompagner
 * @version $Id$
 */
public class Home extends WicketExamplePage
{
	/**
	 * Constructor
	 * 
	 * @param parameters
	 *            Page parameters (ignored since this is the home page)
	 */
	public Home(final PageParameters parameters)
	{
		add(new SelectForm("selectform"));
	}

	class SelectForm extends Form
	{
		SelectModel model;
		Label label;

		/**
		 * Constructor
		 * 
		 * @param name
		 *            Name of form
		 */
		public SelectForm(String name)
		{
			super(name, null);
			model = new SelectModel();
			label = new Label("label", new PropertyModel(model, "name"));
			add(label);
			DropDownChoice choice = new DropDownChoice("users", model, new UserIdList());
			add(choice);
		}

		/**
		 * @see wicket.markup.html.form.Form#onSubmit()
		 */
		public void onSubmit()
		{
			getRequestCycle().setResponsePage(Home.this);
		}
	}

	class SelectModel extends AbstractModel
	{
		private Object selection;

		/**
		 * @see wicket.model.IModel#getObject(Component)
		 */
		public Object getObject(final Component component)
		{
			return selection;
		}

		/**
		 * @see wicket.model.IModel#setObject(Component, Object)
		 */
		public void setObject(final Component component, final Object object)
		{
			selection = object;
		}

		/**
		 * @see wicket.model.IModel#getNestedModel()
		 */
		public Object getNestedModel()
		{
			return selection;
		}
	}

	class UserIdList extends DetachableChoiceList
	{
		/**
		 * @see wicket.markup.html.form.model.DetachableChoiceList#onAttach()
		 */
		public void onAttach()
		{
			if (size() == 0)
			{
				add(new User(new Long(1), "Foo"));
				add(new User(new Long(2), "Bar"));
				add(new User(new Long(3), "FooBar"));
			}
		}
		
		/**
		 * @see wicket.markup.html.form.model.ChoiceList#newChoice(java.lang.Object, int)
		 */
		protected IChoice newChoice(final Object object, final int index)
		{
			final User user = (User)object;
			return new IChoice()
			{
				public String getDisplayValue()
				{
					return user.getName();
				}

				public String getId()
				{
					return user.getId().toString();
				}

				public Object getObject()
				{
					return object;
				}				
			};
		}
	}
}
