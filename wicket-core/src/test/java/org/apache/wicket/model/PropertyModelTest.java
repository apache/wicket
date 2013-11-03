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

import static org.hamcrest.CoreMatchers.instanceOf;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.WicketTestCase;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test class for exercising the {@link PropertyModel}.
 * 
 * @author dashorst
 */
public class PropertyModelTest extends WicketTestCase
{
	/**
	 * Interface for testing the property assignment with an <code>null</code>
	 * interface property.
	 */
	public static interface IAddress
	{
	}

	/**
	 * Abstract class for testing the property assignment with an
	 * <code>null</code> abstract class property.
	 */
	public static abstract class AbstractAddress implements IAddress
	{
		/** street field for assignment in property expressions. */
		public String street;
	}

	/**
	 * Concrete class for testing the property assignment with an
	 * <code>null</code> concrete class property.
	 */
	public static class ConcreteAddress extends AbstractAddress
	{
	}

	/**
	 * Person class for keeping the various different references for use in the
	 * test cases.
	 */
	public static class Person
	{
		/** tests a <code>null</code> interface property. */
		public IAddress interfaceAddress;
		/** tests a <code>null</code> abstract class property. */
		public AbstractAddress abstractAddress;
		/** tests a <code>null</code> concrete class property. */
		public ConcreteAddress concreteAddress;
		/** tests a <code>null</code> final concrete class property. */
		public final ConcreteAddress finalAddress = null;
	}

	/**
	 * Tests setting a value on a {@link PropertyModel} when a property is
	 * <code>null</code> and an interface type. This should end in an exception
	 * because Wicket can't decide what to instantiate on behalf of the program.
	 */
	@Test
	public void setWithNullPathInterface()
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
	 * Tests setting a value on a {@link PropertyModel} when a property is
	 * <code>null</code> and an abstract class type. This should end in an
	 * exception because Wicket can't decide what to instantiate on behalf of
	 * the program.
	 */
	@Test(expected = WicketRuntimeException.class)
	public void setWithNullPathAbstract()
	{
		Person person = new Person();
		PropertyModel<String> model = new PropertyModel<String>(person, "abstractAddress.street");
		model.setObject("foo");
	}

	/**
	 * Tests setting a value on a {@link PropertyModel} when a property is
	 * <code>null</code> and a concrete type. This should work because Wicket
	 * can decide what to instantiate on behalf of the program: the concrete
	 * class.
	 */
	@Test
	public void setWithNullPathConcrete()
	{
		Person person = new Person();
		PropertyModel<String> model = new PropertyModel<String>(person, "concreteAddress.street");
		model.setObject("foo");
		assertThat(person.concreteAddress, instanceOf(ConcreteAddress.class));
		assertEquals("foo", person.concreteAddress.street);
	}

	/**
	 * Tests setting a value on a {@link PropertyModel} when a final (constant!)
	 * property is <code>null</code> and a concrete type. This should end in an
	 * exception because Wicket can't assign to the property, since it is final.
	 * 
	 * This test has been disabled as it doesn't work on Mac OS X's 1.4 jdk
	 * (assignment doesn't fail).
	 */
	@Test(expected = WicketRuntimeException.class)
	@Ignore
	public void setWithNullPathFinalJdk14()
	{
		Person person = new Person();
		PropertyModel<String> model = new PropertyModel<String>(person, "finalAddress.street");
		model.setObject("foo");
		fail("Expected exception");
	}

	/**
	 * Tests setting a value on a null, final property using a
	 * {@link PropertyModel}. This test should pass when run using JDK 1.5 or
	 * newer.
	 */
	@Test
	public void setWithNullPathFinalJdk15()
	{
		Person person = new Person();
		PropertyModel<String> model = new PropertyModel<String>(person, "finalAddress.street");

		model.setObject("foo");
		assertThat(person.finalAddress, instanceOf(ConcreteAddress.class));
		assertEquals("foo", person.finalAddress.street);
	}
}
