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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.model.IModel;
import org.junit.Test;

/**
 * Tests for {@link SupplierModel}
 */
public class SupplierModelTest
{
	@Test
	public void nocaching()
	{
		// given
		Person person = mock(Person.class);
		when(person.getName()).thenReturn("The person's name");
		IModel<String> personNameModel = new SupplierModel<>(person::getName);

		// when

		// once
		personNameModel.getObject();
		personNameModel.getObject();

		personNameModel.detach();
		// twice
		personNameModel.getObject();
		personNameModel.getObject();
		personNameModel.getObject();

		// then
		verify(person, times(5)).getName();
	}

	@Test
	public void serialize()
	{
		Person person = new Person();
		final String personName = "The person's name";
		person.setName(personName);
		final WicketSupplier<String> getName = person::getName;
		IModel<String> personNameModel = new SupplierModel<>(getName);

		final IModel<String> clone = WicketObjects.cloneObject(personNameModel);
		assertThat(clone.getObject(), is(personName));
	}

	@Test
	public void equality()
	{
		Person person = new Person();
		final WicketSupplier<String> getName = person::getName;
		IModel<String> personNameModel1 = new SupplierModel<>(getName);
		IModel<String> personNameModel2 = new SupplierModel<>(getName);
		assertEquals(personNameModel1, personNameModel2);
	}

	@Test
	public void hashcode()
	{
		Person person = new Person();
		final WicketSupplier<String> getName = person::getName;
		IModel<String> personNameModel1 = new SupplierModel<>(getName);
		IModel<String> personNameModel2 = new SupplierModel<>(getName);
		assertEquals(personNameModel1.hashCode(), personNameModel2.hashCode());
	}
}
