package org.apache.wicket.model;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.apache.wicket.model.lambda.Address;
import org.apache.wicket.model.lambda.Person;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link IModel}'s methods
 */
public class IModelTest extends Assert
{
	private Person person;
	private final String name = "John";
	private final String street = "Strasse";

	@Before
	public void before()
	{
		person = new Person();
		person.setName(name);

		Address address = new Address();
		person.setAddress(address);
		address.setStreet(street);
		address.setNumber(123);
	}

	@Test
	public void filterMatch()
	{
		IModel<Person> johnModel = IModel.of(person)
				.filter((p) -> p.getName().equals(name));

		assertThat(johnModel.getObject(), is(person));
	}

	@Test
	public void filterNoMatch()
	{
		IModel<Person> johnModel = IModel.of(person)
				.filter((p) -> p.getName().equals("Jane"));

		assertThat(johnModel.getObject(), is(nullValue()));
	}

	@Test
	public void map()
	{
		IModel<String> personNameModel = IModel.of(person).map(Person::getName);
		assertThat(personNameModel.getObject(), is(equalTo(name)));
	}

	@Test
	public void map2()
	{
		IModel<String> streetModel = IModel.of(person).map(Person::getAddress).map(Address::getStreet);
		assertThat(streetModel.getObject(), is(equalTo(street)));
	}
}
