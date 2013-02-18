package org.apache.wicket.reference.models.wrapped;

import java.util.Locale;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.reference.models.wrapped.PersonModel.PersonModelType;
import org.apache.wicket.util.convert.IConverter;

//#classOnly
public class PersonTableColumnWithCustomLabel extends AbstractColumn<Person, Void>
{
	private final PersonModelType type;

	public PersonTableColumnWithCustomLabel(String columnName, PersonModelType type)
	{
		super(Model.of(columnName));
		this.type = type;
	}

	//#refactor
	@Override
	public void populateItem(Item<ICellPopulator<Person>> cellItem, String componentId,
		IModel<Person> rowModel)
	{

		switch (type)
		{
			// this needs to be handled seperately due to it being an Address
			// instance from the Person object.
			case ADDRESS_MODEL :
				cellItem.add(new CustomLabel(componentId, new PersonModel<Address>(rowModel,
					PersonModelType.ADDRESS_MODEL), new IConverter<Address>()
				{
					@Override
					public Address convertToObject(String value, Locale locale)
					{
						throw new UnsupportedOperationException("converter is readonly.");
					}

					@Override
					public String convertToString(Address address, Locale locale)
					{
						return address.getCity();
					}
				}));
				break;

			default :
				cellItem.add(new Label(componentId, new PersonModel(rowModel, type)));
		}
	}
	//#refactor
}
// #classOnly
