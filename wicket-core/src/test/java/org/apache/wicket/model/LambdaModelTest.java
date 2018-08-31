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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.model.lambda.Person;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LambdaModel}
 */
@SuppressWarnings("javadoc")
class LambdaModelTest
{
	@Test
	void methodReference()
	{
		Person person = new Person();
		IModel<String> personNameModel = LambdaModel.of(person::getName, person::setName);
		check(personNameModel);
	}

	@Test
	void explicitLambdas()
	{
		Person person = new Person();
		IModel<String> personNameModel = LambdaModel.<String>of(
				() -> person.getName(),
				(name) -> person.setName(name));
		check(personNameModel);
	}

	@Test
	void targetModel()
	{
		IModel<Person> target = Model.of(new Person());

		IModel<String> personNameModel = LambdaModel.of(target, Person::getName, Person::setName);
		check(personNameModel);
	}

	@Test
	void targetModelNull()
	{
		IModel<Person> target = Model.of((Person)null);

		IModel<String> personNameModel = LambdaModel.of(target, Person::getName, Person::setName);

		personNameModel.setObject("new name");
		assertNull(personNameModel.getObject());
	}

	@Test
	void targetReadOnly()
	{
		IModel<Person> target = Model.of(new Person());

		IModel<String> personNameModel = LambdaModel.of(target, Person::getName);

		assertThrows(UnsupportedOperationException.class, () -> {
			check(personNameModel);
		});
	}

	private void check(IModel<String> personNameModel)
	{
		assertNull(personNameModel.getObject());

		final String personName = "new name";
		personNameModel.setObject(personName);
		assertEquals(personName, personNameModel.getObject());

		serialize(personNameModel, personName);
	}

	private void serialize(IModel<String> personNameModel, String personName)
	{
		final IModel<String> clone = WicketObjects.cloneObject(personNameModel);
		assertThat(clone).isInstanceOf(LambdaModel.class);
		assertEquals(personName, clone.getObject());
	}
}
