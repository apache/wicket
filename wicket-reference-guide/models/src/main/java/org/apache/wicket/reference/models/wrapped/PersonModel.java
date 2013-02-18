package org.apache.wicket.reference.models.wrapped;

import org.apache.wicket.model.IModel;

public class PersonModel<T> implements IModel<T>
{
	private final IModel<Person> personContainingModel;
	private final PersonModelType type;

	public enum PersonModelType {
		NAME_MODEL, AGE_MODEL, ADDRESS_MODEL;
	}

	public PersonModel(IModel<Person> personContainingModel, PersonModelType type)
	{
		this.personContainingModel = personContainingModel;
		this.type = type;
	}

	@Override
	public T getObject()
	{
		Person person = personContainingModel.getObject();

		switch (type)
		{
			case NAME_MODEL :
				return (T)person.getName();

			case AGE_MODEL :
				return (T)Integer.valueOf(person.getAge());

			case ADDRESS_MODEL :
				return (T)person.getAddress();

			default :
				throw new UnsupportedOperationException("invalid PersonModelType = " + type.name());
		}
	}

	@Override
	public void setObject(T object)
	{
		Person person = personContainingModel.getObject();

		switch (type)
		{
			case NAME_MODEL :
				person.setName((String)object);
				break;

			case AGE_MODEL :
				person.setAge((Integer)object);
				break;

			case ADDRESS_MODEL :
				person.setAddress((Address)object);
				break;

			default :
				throw new UnsupportedOperationException("invalid PersonModelType = " + type.name());
		}
	}

	@Override
	public void detach()
	{
		personContainingModel.detach();
	}
}
