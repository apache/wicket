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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Test class for exercising the {@link PropertyModel}.
 * 
 * @author dashorst
 */
public class PropertyModelTest extends WicketTestCase
{
	/**
	 * Interface for testing the property assignment with an <code>null</code> interface property.
	 */
	public static interface IAddress
	{
	}

	/**
	 * Abstract class for testing the property assignment with an <code>null</code> abstract class
	 * property.
	 */
	public static abstract class AbstractAddress implements IAddress
	{
		/** street field for assignment in property expressions. */
		String street;
	}

	/**
	 * Concrete class for testing the property assignment with an <code>null</code> concrete class
	 * property.
	 */
	public static class ConcreteAddress extends AbstractAddress
	{
	}

	/**
	 * Person class for keeping the various different references for use in the test cases.
	 */
	static class Person
	{
		/** tests a <code>null</code> interface property. */
		public IAddress interfaceAddress;
		/** tests a <code>null</code> abstract class property. */
		public AbstractAddress abstractAddress;
		/** tests a <code>null</code> concrete class property. */
		ConcreteAddress concreteAddress;
		/** tests a <code>null</code> final concrete class property. */
		final ConcreteAddress finalAddress = null;
	}

	/**
	 * Tests setting a value on a {@link PropertyModel} when a property is <code>null</code> and an
	 * interface type. This should end in an exception because Wicket can't decide what to
	 * instantiate on behalf of the program.
	 */
	@Test
	void setWithNullPathInterface()
	{
		Person person = new Person();
		PropertyModel<String> model = new PropertyModel<String>(person, "interfaceAddress.street");
		try
		{
			model.setObject("foo");
			fail("Expected exception");
		}
		catch (WicketRuntimeException wre)
		{
			// ok
		}
	}

	/**
	 * Tests setting a value on a {@link PropertyModel} when a property is <code>null</code> and an
	 * abstract class type. This should end in an exception because Wicket can't decide what to
	 * instantiate on behalf of the program.
	 */
	@Test
	void setWithNullPathAbstract()
	{
		Person person = new Person();
		PropertyModel<String> model = new PropertyModel<String>(person, "abstractAddress.street");
		assertThrows(WicketRuntimeException.class, ()->{
			model.setObject("foo");
		});

	}

	/**
	 * Tests setting a value on a {@link PropertyModel} when a property is <code>null</code> and a
	 * concrete type. This should work because Wicket can decide what to instantiate on behalf of
	 * the program: the concrete class.
	 */
	@Test
	void setWithNullPathConcrete()
	{
		Person person = new Person();
		PropertyModel<String> model = new PropertyModel<String>(person, "concreteAddress.street");
		model.setObject("foo");
		assertNotNull(person.concreteAddress, "concreteAddress");
		assertThat(person.concreteAddress).isInstanceOf(ConcreteAddress.class);
		assertEquals("foo", person.concreteAddress.street);
	}

	/**
	 * Tests setting a value on a null, final property using a {@link PropertyModel}. This test
	 * should pass when run using JDK 1.5 or newer.
	 */
	@Test
	void setWithNullPathFinalJdk15()
	{
		Person person = new Person();
		PropertyModel<String> model = new PropertyModel<String>(person, "finalAddress.street");

		model.setObject("foo");
		assertThat(person.finalAddress).isInstanceOf(ConcreteAddress.class);
		assertEquals("foo", person.finalAddress.street);
	}
}
