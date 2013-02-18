package org.apache.wicket.reference.models.compound;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

public class CompoundModelBindPanel extends Panel
{

	public CompoundModelBindPanel(String id)
	{
		super(id);
		
		Person person = new Person();
		person.setAge(12);
		person.setName("Klaus");
		Address address = new Address();
		address.setCity("Lübeck");
		person.setAddress(address);
		
		//#bind
		CompoundPropertyModel<Person> personModel = new CompoundPropertyModel<Person>(
			person);
		Form<Person> form = new Form<Person>("form", personModel);
		form.add(new RequiredTextField<String>("city", personModel.<String>bind("address.city")));
		//#bind
		
		form.add(new RequiredTextField<String>("name"));
		form.add(new RequiredTextField<Integer>("age", Integer.class));
		
		add(form);
		
	}

}
