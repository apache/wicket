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

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.WicketTestCase;

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
	 * <code>null</code> and an interface type. This should end in an
	 * exception because Wicket can't decide what to instantiate on behalf of
	 * the program.
	 */
	public void testSetWithNullPathInterface()
	{
		Person person = new Person();
		PropertyModel model = new PropertyModel(person, "interfaceAddress.street");
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
	public void testSetWithNullPathAbstract()
	{
		Person person = new Person();
		PropertyModel model = new PropertyModel(person, "abstractAddress.street");
		try
		{
			model.setObject("foo");
			fail("Expected exception");
		}
		catch (WicketRuntimeException wre)
		{
			// ok!
		}
	}

	/**
	 * Tests setting a value on a {@link PropertyModel} when a property is
	 * <code>null</code> and a concrete type. This should work because Wicket
	 * can decide what to instantiate on behalf of the program: the concrete
	 * class.
	 */
	public void testSetWithNullPathConcrete()
	{
		Person person = new Person();
		PropertyModel model = new PropertyModel(person, "concreteAddress.street");
		model.setObject("foo");
		assertNotNull("concreteAddress", person.concreteAddress);
		assertTrue(person.concreteAddress instanceof ConcreteAddress);
		assertEquals("foo", person.concreteAddress.street);
	}

	/**
	 * Tests setting a value on a {@link PropertyModel} when a final (constant!)
	 * property is <code>null</code> and a concrete type. This should end in
	 * an exception because Wicket can't assign to the property, since it is
	 * final.
	 * 
	 * This test has been disabled as it doesn't work on Mac OS X's 1.4 jdk
	 * (assignment doesn't fail).
	 */
// public void testSetWithNullPathFinalJdk14()
// {
// Person person = new Person();
// PropertyModel model = new PropertyModel(person, "finalAddress.street");
//
// try
// {
// model.setObject("foo");
// fail("Expected exception");
// }
// catch (WicketRuntimeException wre)
// {
// // ok!
// }
// }
	/**
	 * Tests setting a value on a null, final property using a
	 * {@link PropertyModel}. This test should pass when run using JDK 1.5 or
	 * newer.
	 * 
	 * TODO/FIXME enable test when Wicket is JDK 1.5 based.
	 */
// public void testSetWithNullPathFinalJdk15()
// {
// Person person = new Person();
// PropertyModel model = new PropertyModel(person, "finalAddress.street");
//
// model.setObject("foo");
// assertNotNull("finalAddress", person.finalAddress);
// assertTrue(person.finalAddress instanceof ConcreteAddress);
// assertEquals("foo", person.finalAddress.street);
// }
}
