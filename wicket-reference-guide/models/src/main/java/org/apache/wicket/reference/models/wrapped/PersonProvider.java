package org.apache.wicket.reference.models.wrapped;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class PersonProvider implements IDataProvider<Person>
{
	@Override
	public void detach()
	{

	}

	@Override
	public Iterator<? extends Person> iterator(long first, long count)
	{
		return new ArrayList<Person>().iterator();
	}

	@Override
	public long size()
	{
		return 0;
	}

	@Override
	public IModel<Person> model(Person person)
	{
		return Model.of(person);
	}

}
