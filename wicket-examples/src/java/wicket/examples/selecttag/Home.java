package wicket.examples.selecttag;

import java.util.ArrayList;
import java.util.Iterator;

import wicket.PageParameters;
import wicket.examples.util.NavigationPanel;
import wicket.markup.html.HtmlPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.IDetachableChoiceList;
import wicket.model.IModel;

/**
 * @author jcompagner
 * @version $Id$
 */
public class Home extends HtmlPage
{
	/**
	 * Constructor
	 * 
	 * @param parameters
	 *          Page parameters (ignored since this is the home page)
	 */
	public Home(final PageParameters parameters)
	{
        add(new NavigationPanel("mainNavigation", "Helloworld example"));
		add(new SelectForm("selectform"));
	}
	
	class SelectForm extends Form
	{
		SelectModel model;
		Label label;
		
		public SelectForm(String name)
		{
			super(name,null);
			model = new SelectModel();
			label = new Label("label",model,"name");
			add(label);
			DropDownChoice choice = new DropDownChoice("users",model,new UserIdList());
			add(choice);
		}

		/*
		 * @see wicket.markup.html.form.Form#handleSubmit()
		 */
		public void handleSubmit()
		{
			getRequestCycle().setRedirect(true);
			getRequestCycle().setPage(Home.this);
		}
	}
	
	class SelectModel implements IModel
	{
		private Object selection; 
		/*
		 * @see wicket.model.IModel#getObject()
		 */
		public Object getObject()
		{
			return selection;
		}

		/*
		 * @see wicket.model.IModel#setObject(java.lang.Object)
		 */
		public void setObject(Object object)
		{
			selection = object;
		}
	}
	
	class UserIdList extends ArrayList implements IDetachableChoiceList
	{
		/*
		 * @see wicket.markup.html.form.IDetachableChoiceList#detach()
		 */
		public void detach()
		{
			this.clear();
		}

		/*
		 * @see wicket.markup.html.form.IDetachableChoiceList#attach()
		 */
		public void attach()
		{
			if(size() == 0)
			{
				add(new User(new Long(1),"Foo"));
				add(new User(new Long(2),"Bar"));
				add(new User(new Long(3),"FooBar"));
			}
		}

		/*
		 * @see wicket.markup.html.form.IDetachableChoiceList#getDisplayValue(int)
		 */
		public String getDisplayValue(int row)
		{
			return ((User)get(row)).getName();
		}

		/*
		 * @see wicket.markup.html.form.IDetachableChoiceList#getIdValue(int)
		 */
		public String getId(int row)
		{
			return ((User)get(row)).getId().toString();
		}

		/*
		 * @see wicket.markup.html.form.IDetachableChoiceList#getObjectById(java.lang.String)
		 */
		public Object objectForId(String id)
		{
			Long longId = new Long(id);
			Iterator it = iterator();
			while(it.hasNext())
			{
				User user = (User)it.next();
				if(user.getId().equals(longId))
				{
					return user;
				}
			}
			return null;
		}
	}
}
