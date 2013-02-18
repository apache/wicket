package org.apache.wicket.reference.models.wrapped;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.reference.models.wrapped.AddressModel.AddressModelType;
import org.apache.wicket.reference.models.wrapped.PersonModel.PersonModelType;

public class WrappedModelFormPage extends WebPage
{
	public WrappedModelFormPage()
	{
		//#form
		Person person = new Person();
		Model<Person> beanModel = Model.of(person);
		
		//#form
		person.setAge(12);
		person.setName("Klaus");
		Address address = new Address();
		address.setCity("Lübeck");
		person.setAddress(address);
		//#form
		Form<Void> form = new Form<Void>("form");
		
		form.add(new RequiredTextField<String>("name", new PersonModel<String>(beanModel,
			PersonModelType.NAME_MODEL)));
		form.add(new RequiredTextField<Integer>("age", new PersonModel<Integer>(beanModel,
			PersonModelType.AGE_MODEL), Integer.class));
		form.add(new RequiredTextField<String>("address.city", new AddressModel<String>(
			new PersonModel<Address>(beanModel, PersonModelType.ADDRESS_MODEL),
			AddressModelType.CITY_MODEL)));

		add(form);
		//#form

		//#datatable
		List<IColumn<Person,Void>> columns=new ArrayList<IColumn<Person,Void>>();
		
		columns.add(new PersonTableColumn("Name", PersonModelType.NAME_MODEL));
		columns.add(new PersonTableColumn("Age", PersonModelType.AGE_MODEL));
		columns.add(new PersonTableColumn("City", PersonModelType.ADDRESS_MODEL));
		
		DataTable<Person, Void> table = new DataTable<Person, Void>("datatable", columns, new PersonProvider(), 10);
		//#datatable
	}
}
