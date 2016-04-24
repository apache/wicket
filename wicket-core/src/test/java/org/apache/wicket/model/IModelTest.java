package org.apache.wicket.model;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.apache.wicket.model.lambda.Person;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link IModel}'s methods
 */
public class IModelTest extends Assert
{
	@Test
	public void filterMatch()
	{
		Person person = new Person();
		String name = "john";
		person.setName(name);

		IModel<Person> johnModel = IModel.of(person)
				.filter((p) -> p.getName().equals(name));

		assertThat(johnModel.getObject(), is(person));
	}

	@Test
	public void filterNoMatch()
	{
		Person person = new Person();
		String name = "john";
		person.setName(name);

		IModel<Person> johnModel = IModel.of(person)
				.filter((p) -> p.getName().equals("jane"));

		assertThat(johnModel.getObject(), is(nullValue()));
	}
}
