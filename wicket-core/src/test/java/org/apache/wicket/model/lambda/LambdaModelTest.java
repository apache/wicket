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
package org.apache.wicket.model.lambda;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.lambdas.WicketConsumer;
import org.apache.wicket.lambdas.WicketSupplier;
import org.apache.wicket.model.IModel;
import org.junit.Test;

/**
 * Tests for {@link LambdaModel}
 */
public class LambdaModelTest
{
	@Test
	public void methodReference()
	{
		Person person = new Person();
		IModel<String> personNameModel = new LambdaModel<>(person::getName, person::setName);
		check(personNameModel);
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
