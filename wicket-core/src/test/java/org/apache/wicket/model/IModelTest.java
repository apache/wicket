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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.Serializable;

import org.apache.wicket.lambda.WicketBiFunction;
import org.apache.wicket.model.lambda.Address;
import org.apache.wicket.model.lambda.Person;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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

	@Test
	public void withMap()
	{
		IModel<String> janeModel = IModel.of("Jane");
		WicketBiFunction<Person, String, String> function =
				(WicketBiFunction<Person, String, String>) (person1, nth) ->
						person1.getName() + " is in relationship with " + nth;
		IModel<String> relationShipModel = IModel.of(person).mapWith(function, janeModel);
		assertThat(relationShipModel.getObject(), is(equalTo("John is in relationship with Jane")));
	}

	@Test
	public void withMapWithNullObject()
	{
		IModel<String> janeModel = IModel.of(null);
		WicketBiFunction<Person, String, String> function =
				(WicketBiFunction<Person, String, String>) (person1, nth) ->
						person1.getName() + " is in relationship with " + nth;
		IModel<String> relationShipModel = IModel.of(person).mapWith(function, janeModel);
		assertThat(relationShipModel.getObject(), is(nullValue()));
	}

	@Test
	public void withMapWithNullModel()
	{
		IModel<String> janeModel = null;
		WicketBiFunction<Person, String, String> function =
				(WicketBiFunction<Person, String, String>) (person1, nth) ->
						person1.getName() + " is in relationship with " + nth;
		IModel<String> relationShipModel = IModel.of(person).mapWith(function, janeModel);
		assertThat(relationShipModel.getObject(), is(nullValue()));
	}

	@Ignore
	@Test
	public void flatMap()
	{
		IModel<String> heirModel = IModel.of(person).flatMap(person1 -> new Model<String>() {
			@Override
			public String getObject()
			{
				return person1.getName() + " is my parent";
			}
		});
		assertThat(heirModel.getObject(), is(equalTo("John is my parent")));

		String newValue = "New Value";
		heirModel.setObject(newValue);
		assertThat(heirModel.getObject(), is(equalTo(newValue)));
	}
}
