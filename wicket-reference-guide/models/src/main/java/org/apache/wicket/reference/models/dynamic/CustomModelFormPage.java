package org.apache.wicket.reference.models.dynamic;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.Model;

public class CustomModelFormPage extends WebPage
{
	public CustomModelFormPage()
	{
		final PersonBean person = new PersonBean();

		Form<Void> personForm = new Form<Void>("form");

		//#customModel
		personForm.add(new RequiredTextField<String>("personName", new Model<String>()
		{
			@Override
			public String getObject()
			{
				return person.getName();
			}

			@Override
			public void setObject(String object)
			{
				person.setName(object);
			}
		}));
		//#customModel

		add(personForm);
	}
}
