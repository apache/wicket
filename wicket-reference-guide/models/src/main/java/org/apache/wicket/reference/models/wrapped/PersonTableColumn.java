package org.apache.wicket.reference.models.wrapped;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.reference.models.wrapped.AddressModel.AddressModelType;
import org.apache.wicket.reference.models.wrapped.PersonModel.PersonModelType;

//#classOnly
public class PersonTableColumn extends AbstractColumn<Person, Void>
{
	private final PersonModelType type;

	public PersonTableColumn(String columnName, PersonModelType type)
	{
		super(Model.of(columnName));
		this.type = type;
	}

	@Override
	public void populateItem(Item<ICellPopulator<Person>> cellItem, String componentId,
		IModel<Person> rowModel)
	{

		switch (type)
		{
			// this needs to be handled seperately due to it being an Address
			// instance from the Person object.
			case ADDRESS_MODEL :
				cellItem.add(new Label(componentId, new AddressModel<String>(
					new PersonModel<Address>(rowModel, PersonModelType.ADDRESS_MODEL),
					AddressModelType.CITY_MODEL)));
				break;

			default :
				cellItem.add(new Label(componentId, new PersonModel(rowModel, type)));
		}
	}
}
//#classOnly
