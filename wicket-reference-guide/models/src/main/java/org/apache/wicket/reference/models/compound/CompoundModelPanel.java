package org.apache.wicket.reference.models.compound;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

public class CompoundModelPanel extends Panel
{

	public CompoundModelPanel(String id)
	{
		super(id);
		
		Person person = new Person();
		person.setAge(12);
		person.setName("Klaus");
		Address address = new Address();
		address.setCity("Lübeck");
		person.setAddress(address);
		//#form
		Form<Person> form = new Form<Person>("form", new CompoundPropertyModel<Person>(
			person));
		form.add(new RequiredTextField<String>("name"));
		form.add(new RequiredTextField<Integer>("age", Integer.class));
		//#form

		//#addressCity
		form.add(new RequiredTextField<String>("address.city"));
		//#addressCity

		add(form);
		
	}

}
