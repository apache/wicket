/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.model;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.lambda.WicketConsumer;
import org.apache.wicket.lambda.WicketSupplier;
import org.apache.wicket.model.lambda.Person;
import org.junit.Test;

/**
 * Tests for {@link LambdaModel}
 */
@SuppressWarnings("javadoc")
public class LambdaModelTest
{
	@Test
	public void methodReference()
	{
		Person person = new Person();
		person.setName("john");
		Person.Address address = new Person.Address();
		person.setAddress(address);
//		address.setStreet("Street");
		address.setNumber(123);

		IModel<String> personNameModel = new LambdaModel<>(person::getName, person::setName);
//		check(personNameModel);

		IModel<Person.Address> addressModel = Model.<Person>of(Model.of(person))
					.filter((p) -> p.getName().equals("john"))
					.map(Person::getAddress);

		System.err.println("addressModel= " + addressModel
					.flatMap(address1 -> Model.loadableDetachable(() -> address1))
					.map(Person.Address::getStreet)
					.orElse("N/A")
					.getObject());
	}

	@Test
	public void explicitLambdas()
	{
		Person person = new Person();
		IModel<String> personNameModel = new LambdaModel<>(
				() -> person.getName(),
				(name) -> person.setName(name));
		check(personNameModel);
	}

	@Test
	public void targetModel()
	{
		IModel<Person> target = Model.of(new Person());

		IModel<String> personNameModel = LambdaModel.of(target, Person::getName, Person::setName);
		check(personNameModel);
	}

	@Test
	public void targetModelNull()
	{
		IModel<Person> target = Model.of((Person)null);

		IModel<String> personNameModel = LambdaModel.of(target, Person::getName, Person::setName);

		personNameModel.setObject("new name");
		assertThat(personNameModel.getObject(), is(nullValue()));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void targetReadOnly()
	{
		IModel<Person> target = Model.of(new Person());

		IModel<String> personNameModel = LambdaModel.of(target, Person::getName);
		check(personNameModel);
	}

	@Test
	public void equality()
	{
		Person person = new Person();
		final WicketSupplier<String> getName = person::getName;
		final WicketConsumer<String> setName = person::setName;
		IModel<String> personNameModel1 = new LambdaModel<>(getName, setName);
		IModel<String> personNameModel2 = new LambdaModel<>(getName, setName);
		assertEquals(personNameModel1, personNameModel2);
	}

	@Test
	public void hashcode()
	{
		Person person = new Person();
		final WicketSupplier<String> getName = person::getName;
		final WicketConsumer<String> setName = person::setName;
		IModel<String> personNameModel1 = new LambdaModel<>(getName, setName);
		IModel<String> personNameModel2 = new LambdaModel<>(getName, setName);
		assertEquals(personNameModel1.hashCode(), personNameModel2.hashCode());
	}

	private void check(IModel<String> personNameModel)
	{
		assertThat(personNameModel.getObject(), is(nullValue()));

		final String personName = "new name";
		personNameModel.setObject(personName);
		assertThat(personNameModel.getObject(), is(personName));

		serialize(personNameModel, personName);
	}

	private void serialize(IModel<String> personNameModel, String personName)
	{
		final IModel<String> clone = WicketObjects.cloneObject(personNameModel);
		assertThat(clone, is(instanceOf(LambdaModel.class)));
		assertThat(clone.getObject(), is(personName));
	}

}
