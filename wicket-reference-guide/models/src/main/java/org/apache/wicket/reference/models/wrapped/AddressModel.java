package org.apache.wicket.reference.models.wrapped;

import org.apache.wicket.model.IModel;

//#classOnly
public class AddressModel<T> implements IModel<T>
{
	private final IModel<Address> addressContainingModel;

	private final AddressModelType type;

	public enum AddressModelType {
		CITY_MODEL;
	};

	public AddressModel(IModel<Address> addressContainingModel, AddressModelType type)
	{
		this.addressContainingModel = addressContainingModel;
		this.type = type;
	}

	@Override
	public T getObject()
	{
		Address address = addressContainingModel.getObject();
		switch (type)
		{
			case CITY_MODEL :
				return (T)address.getCity();
			default :
				throw new UnsupportedOperationException("invalid AddressModelType = " + type.name());
		}
	}

	@Override
	public void setObject(T object)
	{
		Address address = addressContainingModel.getObject();
		switch (type)
		{
			case CITY_MODEL :
				address.setCity((String)object);
				break;
			default :
				throw new UnsupportedOperationException("invalid AddressModelType = " + type.name());
		}
	}

	@Override
	public void detach()
	{
		addressContainingModel.detach();
	}
}
//#classOnly
