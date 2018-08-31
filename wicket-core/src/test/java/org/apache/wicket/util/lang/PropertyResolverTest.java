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
package org.apache.wicket.util.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.wicket.ConverterLocator;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.core.util.lang.PropertyResolver.AbstractGetAndSet;
import org.apache.wicket.core.util.lang.PropertyResolver.CachingPropertyLocator;
import org.apache.wicket.core.util.lang.PropertyResolver.DefaultPropertyLocator;
import org.apache.wicket.core.util.lang.PropertyResolver.IGetAndSet;
import org.apache.wicket.core.util.lang.PropertyResolver.IPropertyLocator;
import org.apache.wicket.core.util.lang.PropertyResolverConverter;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.AbstractConverter;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author jcompagner
 * 
 */
public class PropertyResolverTest extends WicketTestCase
{

	private static final PropertyResolverConverter CONVERTER = new PropertyResolverConverter(
		new ConverterLocator(), Locale.US);

	private Person person;

	/**
	 * @throws Exception
	 */
	@BeforeEach
	void before()
	{
		person = new Person();
	}

	/**
	 * @throws Exception
	 */
	@AfterEach
	void after()
	{
		PropertyResolver.destroy(tester.getApplication());
	}

	/**
	 * @throws Exception
	 */
	@Test
	void simpleExpression() throws Exception
	{
		String name = (String) PropertyResolver.getValue("name", person);
		assertNull(name);

		PropertyResolver.setValue("name", person, "wicket", CONVERTER);
		name = (String)PropertyResolver.getValue("name", person);
		assertEquals(name, "wicket");
	}

	/**
	 * @throws Exception
	 */
	@Test
	void primitiveValue() throws Exception
	{
		Integer integer = (Integer)PropertyResolver.getValue("age", person);
		assertTrue(integer == 0);

		PropertyResolver.setValue("age", person, 10, CONVERTER);
		integer = (Integer)PropertyResolver.getValue("age", person);
		assertTrue(integer == 10);

		assertThrows(ConversionException.class, () -> {
			PropertyResolver.setValue("age", person, null, CONVERTER);
		});
	}

	/**
	 * @throws Exception
	 */
	@Test
	void pathExpression() throws Exception
	{
		person.setAddress(new Address());
		PropertyResolver.setValue("address.street", person, "wicket-street", CONVERTER);
		String street = (String)PropertyResolver.getValue("address.street", person);
		assertEquals(street, "wicket-street");

	}

	/**
	 * @throws Exception
	 */
	@Test
	void testNull() throws Exception
	{
		String street = (String)PropertyResolver.getValue("address.street", person);
		assertNull(street);
	}

	/**
	 * @throws Exception
	 */
	@Test
	void nullCreation() throws Exception
	{
		PropertyResolver.setValue("address.street", person, "wicket-street", CONVERTER);
		String street = (String)PropertyResolver.getValue("address.street", person);
		assertEquals(street, "wicket-street");

		try
		{
			PropertyResolver.setValue("country.name", person, "US", CONVERTER);
			fail("name can't be set on a country that doesn't have default constructor");
		}
		catch (WicketRuntimeException ex)
		{
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	void getterOnly() throws Exception
	{
		PropertyResolver.setValue("country", person, new Country("US"), CONVERTER);
		PropertyResolver.getValue("country.name", person);

		try
		{
			PropertyResolver.setValue("country.name", person, "NL", CONVERTER);
		}
		catch (WicketRuntimeException ex)
		{
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	void pathExpressionWithConversion() throws Exception
	{
		person.setAddress(new Address());
		PropertyResolver.setValue("address.number", person, "10", CONVERTER);
		Integer number = (Integer)PropertyResolver.getValue("address.number", person);
		assertEquals(number, Integer.valueOf(10));

		try
		{
			PropertyResolver.setValue("address.number", person, "10a", CONVERTER);
			throw new Exception("Conversion error should be thrown");
		}
		catch (ConversionException ex)
		{
		}

	}

	/**
	 * @throws Exception
	 */
	@Test
	void mapLookup() throws Exception
	{
		Address address = new Address();
		PropertyResolver.setValue("addressMap", person, new HashMap<String, Address>(), CONVERTER);
		PropertyResolver.setValue("addressMap.address", person, address, CONVERTER);
		PropertyResolver.setValue("addressMap.address.street", person, "wicket-street", CONVERTER);
		String street = (String)PropertyResolver.getValue("addressMap.address.street", person);
		assertEquals(street, "wicket-street");
	}

	/**
	 * @throws Exception
	 */
	@Test
	void mapWithDotLookup() throws Exception
	{
		Address address = new Address();
		HashMap<String, Address> hm = new HashMap<String, Address>();
		PropertyResolver.setValue("addressMap", person, hm, CONVERTER);
		PropertyResolver.setValue("addressMap[address.test]", person, address, CONVERTER);
		assertNotNull(hm.get("address.test"));
		PropertyResolver.setValue("addressMap[address.test].street", person, "wicket-street",
			CONVERTER);
		String street = (String)PropertyResolver.getValue("addressMap[address.test].street", person);
		assertEquals(street, "wicket-street");
	}

	/**
	 * @throws Exception
	 */
	@Test
	void listLookup() throws Exception
	{
		PropertyResolver.setValue("addressList", person, new ArrayList<Address>(), CONVERTER);
		PropertyResolver.setValue("addressList.0", person, new Address(), CONVERTER);
		PropertyResolver.setValue("addressList.10", person, new Address(), CONVERTER);
		PropertyResolver.setValue("addressList.1", person, new Address(), CONVERTER);
		PropertyResolver.setValue("addressList.1.street", person, "wicket-street", CONVERTER);

		String street = (String)PropertyResolver.getValue("addressList.0.street", person);
		assertNull(street);
		street = (String)PropertyResolver.getValue("addressList.1.street", person);
		assertEquals(street, "wicket-street");
	}

	/**
	 * @throws Exception
	 */
	@Test
	void arrayLookup() throws Exception
	{
		PropertyResolver.setValue("addressArray", person, new Address[] { new Address(), null },
			CONVERTER);
		PropertyResolver.setValue("addressArray.0.street", person, "wicket-street", CONVERTER);
		String street = (String)PropertyResolver.getValue("addressArray.0.street", person);
		assertEquals(street, "wicket-street");

		PropertyResolver.setValue("addressArray.1.street", person, "wicket-street", CONVERTER);
		street = (String)PropertyResolver.getValue("addressArray.1.street", person);
		assertEquals(street, "wicket-street");
	}

	/**
	 * @throws Exception
	 */
	@Test
	void arrayLookupByBrackets() throws Exception
	{
		PropertyResolver.setValue("addressArray", person, new Address[] { new Address(), null },
			CONVERTER);
		PropertyResolver.setValue("addressArray[0].street", person, "wicket-street", CONVERTER);
		String street = (String)PropertyResolver.getValue("addressArray[0].street", person);
		assertEquals(street, "wicket-street");

		PropertyResolver.setValue("addressArray[1].street", person, "wicket-street", CONVERTER);
		street = (String)PropertyResolver.getValue("addressArray[1].street", person);
		assertEquals(street, "wicket-street");
	}

	/**
	 * @throws Exception
	 */
	@Test
	void propertyByIndexLookup() throws Exception
	{
		PropertyResolver.setValue("addressAt.0", person, new Address(), CONVERTER);
		PropertyResolver.setValue("addressAt.0.street", person, "wicket-street", CONVERTER);
		String street = (String)PropertyResolver.getValue("addressAt.0.street", person);
		assertEquals(street, "wicket-street");
	}

	/**
	 * @throws Exception
	 */
	@Test
	void getPropertyByNotExistingIndexArrayLookup() throws Exception
	{
		PropertyResolver.setValue("addressArray", person, new Address[] { }, CONVERTER);
		String street = (String)PropertyResolver.getValue("addressArray.0.street", person);
		assertNull(street);
		street = (String)PropertyResolver.getValue("addressArray[0].street", person);
		assertNull(street);
	}

	/**
	 * @throws Exception
	 */
	@Test
	void getPropertyByNotExistingIndexListLookup() throws Exception
	{
		PropertyResolver.setValue("addressList", person, new ArrayList<Address>(), CONVERTER);
		String street = (String)PropertyResolver.getValue("addressList.0.street", person);
		assertNull(street);
		street = (String)PropertyResolver.getValue("addressList[0].street", person);
		assertNull(street);
	}

	/**
	 * @throws Exception
	 */
	@Test
	void getIndexPropertyDirectly() throws Exception
	{
		Address address = new Address();
		Address[] addresses = new Address[] { address };

		Address address2 = (Address)PropertyResolver.getValue("[0]", addresses);
		assertSame(address, address2);
	}

	/**
	 * @throws Exception
	 */
	@Test
	void listSizeLookup() throws Exception
	{
		List<Address> addresses = new ArrayList<Address>();
		addresses.add(new Address());
		addresses.add(new Address());
		person.setAddressList(addresses);
		Object size = PropertyResolver.getValue("addressList.size", person);
		assertEquals(size, 2);
		size = PropertyResolver.getValue("addressList.size()", person);
		assertEquals(size, 2);
	}


	/**
	 * @throws Exception
	 */
	@Test
	void mapSizeLookup() throws Exception
	{
		Map<String, Address> addresses = new HashMap<String, Address>();
		Address address = new Address();
		addresses.put("size", address);
		addresses.put("test", new Address());
		person.setAddressMap(addresses);
		Object addressFromMap = PropertyResolver.getValue("addressMap.size", person);
		assertEquals(addressFromMap, address);
		Object size = PropertyResolver.getValue("addressMap.size()", person);
		assertEquals(size, 2);
	}

	/**
	 * @throws Exception
	 */
	@Test
	void arraySizeLookup() throws Exception
	{
		person.setAddressArray(new Address[] { new Address(), new Address() });
		Object size = PropertyResolver.getValue("addressArray.length", person);
		assertEquals(size, 2);
		size = PropertyResolver.getValue("addressArray.size", person);
		assertEquals(size, 2);
	}

	/**
	 * @throws Exception
	 */
	@Test
	void methodLookup() throws Exception
	{
		Address[] addresses = new Address[] { new Address(), new Address() };
		person.setAddressArray(addresses);
		Object value = PropertyResolver.getValue("getAddressArray()", person);
		assertEquals(value, addresses);
	}

	/**
	 * @throws Exception
	 */
	@Test
	void field() throws Exception
	{
		Address address = new Address();
		PropertyResolver.setValue("address2", person, address, CONVERTER);
		Address address2 = (Address)PropertyResolver.getValue("address2", person);
		assertEquals(address, address2);

		try
		{
			PropertyResolver.setValue("address3", person, address, CONVERTER);
			fail("Shoudln't come here");
		}
		catch (RuntimeException ex)
		{

		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	void testPrivateField() throws Exception
	{
		Address address = new Address();
		PropertyResolver.setValue("privateAddress", person, address, CONVERTER);
		Address address2 = (Address)PropertyResolver.getValue("privateAddress", person);
		assertEquals(address, address2);
	}

	/**
	 * @throws Exception
	 */
	@Test
	void privateFieldOfSuperClass() throws Exception
	{
		Person2 person2 = new Person2();
		Address address = new Address();
		PropertyResolver.setValue("privateAddress", person2, address, CONVERTER);
		Address address2 = (Address)PropertyResolver.getValue("privateAddress", person2);
		assertEquals(address, address2);
	}

	/**
	 * 
	 */
	@Test
	void getTargetClass()
	{
		Address address = new Address();

		Class<?> clazz = PropertyResolver.getPropertyClass("number", address);
		assertEquals(int.class, clazz);

		Person person = new Person();
		person.setAddress(new Address());

		clazz = PropertyResolver.getPropertyClass("address.number", person);
		assertEquals(int.class, clazz);

		person.setAddressArray(new Address[] { new Address(), new Address() });
		clazz = PropertyResolver.getPropertyClass("addressArray[0]", person);
		assertEquals(Address.class, clazz);

		clazz = PropertyResolver.getPropertyClass("addressArray[0].number", person);
		assertEquals(int.class, clazz);
	}

	/**
	 * 
	 */
	@Test
	void getTargetField()
	{
		Address address = new Address();

		Field field = PropertyResolver.getPropertyField("number", address);
		assertEquals(field.getName(), "number");
		assertEquals(field.getType(), int.class);

		Person person = new Person();
		person.setAddress(new Address());

		field = PropertyResolver.getPropertyField("address.number", person);
		assertEquals(field.getName(), "number");
		assertEquals(field.getType(), int.class);

		person.setAddressArray(new Address[] { new Address(), new Address() });
		field = PropertyResolver.getPropertyField("addressArray[0].number", person);
		assertEquals(field.getName(), "number");
		assertEquals(field.getType(), int.class);
	}

	/**
	 * 
	 */
	@Test
	void getTargetGetter()
	{
		Address address = new Address();

		Method method = PropertyResolver.getPropertyGetter("number", address);
		assertEquals(method.getName(), "getNumber");
		assertEquals(method.getReturnType(), int.class);

		Person person = new Person();
		person.setAddress(new Address());

		method = PropertyResolver.getPropertyGetter("address.number", person);
		assertEquals(method.getName(), "getNumber");
		assertEquals(method.getReturnType(), int.class);

		person.setAddressArray(new Address[] { new Address(), new Address() });
		method = PropertyResolver.getPropertyGetter("addressArray[0].number", person);
		assertEquals(method.getName(), "getNumber");
		assertEquals(method.getReturnType(), int.class);
	}

	/**
	 * @throws Exception
	 */
	@Test
	void onlyPrimitiveGetter() throws Exception
	{
		Person person = new Person();

		PropertyResolver.setValue("onlyGetterPrimitive", person, 1, CONVERTER);

		assertEquals(person.getOnlyGetterPrimitive(), 1);
		assertEquals(PropertyResolver.getValue("onlyGetterPrimitive", person), 1);

	}

	/**
	 * @throws Exception
	 */
	@Test
	void onlyStringGetter() throws Exception
	{
		Person person = new Person();

		PropertyResolver.setValue("onlyGetterString", person, "onlygetter", CONVERTER);

		assertEquals(person.getOnlyGetterString(), "onlygetter");
		assertEquals(PropertyResolver.getValue("onlyGetterString", person), "onlygetter");

	}

	/**
	 * 
	 */
	@Test
	void getTargetSetter()
	{
		Address address = new Address();

		Method method = PropertyResolver.getPropertySetter("number", address);
		assertEquals(method.getName(), "setNumber");

		Person person = new Person();
		person.setAddress(new Address());

		method = PropertyResolver.getPropertySetter("address.number", person);
		assertEquals(method.getName(), "setNumber");

		person.setAddressArray(new Address[] { new Address(), new Address() });
		method = PropertyResolver.getPropertySetter("addressArray[0].number", person);
		assertEquals(method.getName(), "setNumber");
	}

	/**
	 * @throws Exception
	 */
	@Test
	void overriddenGetter() throws Exception
	{
		Person2 person = new Person2();
		person.setName("foo");

		String name = (String)PropertyResolver.getValue("name", person);
		assertEquals("foo", name);

		PropertyResolver.setValue("name", person, "bar", CONVERTER);

		name = (String)PropertyResolver.getValue("name", person);
		assertEquals("bar", name);

	}

	/**
	 * @throws Exception
	 */
	@Test
	void propertyClassWithSubType() throws Exception
	{
		Person person = new Person();
		assertEquals(String.class, PropertyResolver.getPropertyClass("country.name", person));
		try
		{
			PropertyResolver.getPropertyClass("country.subCountry.name", person);
			fail("country.subCountry shouldnt be found");
		}
		catch (Exception e)
		{

		}
		person.setCountry(new Country2("test", new Country("test")));
		PropertyResolver.getPropertyClass("country.subCountry.name", person);
	}

	/**
	 * Used for models in testing.
	 */
	private static class InnerVectorPOJO extends Vector<Void>
	{
		private static final long serialVersionUID = 1L;

		/**
		 */
		@SuppressWarnings("unused")
		public String testValue = "vector";
	}

	/**
	 * Tests the PropertyModel with vector.
	 */
	@Test
	void propertyModel()
	{
		String value = (String)PropertyResolver.getValue("testValue", new InnerVectorPOJO());
		assertEquals("vector", value);
	}

	/**
	 * 
	 */
	@Test
	void directFieldSetWithDifferentTypeThanGetter()
	{
		final DirectFieldSetWithDifferentTypeThanGetter obj = new DirectFieldSetWithDifferentTypeThanGetter();
		PropertyResolver.setValue("value", obj, 1, null);
		assertEquals(1, obj.value);
	}

	private static class DirectFieldSetWithDifferentTypeThanGetter
	{
		private int value;

		@SuppressWarnings("unused")
		public String getValue()
		{
			return String.valueOf(value);
		}
	}

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-1802">WICKET-1802</a>
	 */
	@Test
	void conversionExceptionMessageContainsTheObjectPropertyBeingSet()
	{
		try
		{
			PropertyResolverConverter convertToNull = new PropertyResolverConverter(null, null)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public <T> T convert(Object object, java.lang.Class<T> clz)
				{
					return null;
				}
			};
			PropertyResolver.setValue("name", person, "", convertToNull);
			fail("Should have thrown an ConversionException");
		}
		catch (ConversionException e)
		{
			assertTrue(e.getMessage().toLowerCase().contains("name"));
		}
	}

	/**
	 * WICKET-3441
	 */
	@Test
	void dateToStringConverting()
	{
		IConverterLocator converterLocator = new ConverterLocator();
		Locale locale = Locale.GERMAN;
		PropertyResolverConverter converter = new PropertyResolverConverter(converterLocator,
			locale);

		Calendar calDate = Calendar.getInstance();
		calDate.clear();
		calDate.set(2011, Calendar.APRIL, 17);
		Date date = calDate.getTime();

		Object actual = converter.convert(date, String.class);
		String expected = converterLocator.getConverter(Date.class).convertToString(date, locale);
		assertEquals(expected, actual);
	}

	/**
	 * WICKET-3441
	 */
	@Test
	void dateToLongConverting()
	{
		ConverterLocator converterLocator = new ConverterLocator();
		final IConverter<Date> dateConverter = converterLocator.get(Date.class);
		IConverter<Long> customLongConverter = new AbstractConverter<Long>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Long convertToObject(String value, Locale locale)
			{
				Date date = dateConverter.convertToObject(value, locale);
				return date != null ? date.getTime() : null;
			}

			@Override
			public String convertToString(Long value, Locale locale)
			{
				Date date;
				if (value != null)
				{
					date = new Date();
					date.setTime(value);
				}
				else
				{
					date = null;
				}

				return dateConverter.convertToString(date, locale);
			}

			@Override
			protected Class<Long> getTargetType()
			{
				return Long.class;
			}
		};
		converterLocator.set(Long.class, customLongConverter);
		converterLocator.set(Long.TYPE, customLongConverter);

		PropertyResolverConverter converter = new PropertyResolverConverter(converterLocator,
			Locale.ENGLISH);

		Calendar calDate = Calendar.getInstance();
		calDate.clear();
		calDate.set(2011, Calendar.APRIL, 17);
		Date date = calDate.getTime();

		Object actual = converter.convert(date, Long.class);
		assertEquals(date.getTime(), actual);
	}
	
	/**
	 * WICKET-5623 custom properties
	 */
	@Test
	void custom() {
		Document document = new Document();
		document.setType("type");
		document.setProperty("string", "string");
		
		Document nestedCustom = new Document();
		nestedCustom.setProperty("string", "string2");
		document.setProperty("nested", nestedCustom);
		
		PropertyResolver.setLocator(tester.getApplication(), new CachingPropertyLocator(new CustomGetAndSetLocator()));
		
		assertEquals("type", PropertyResolver.getValue("type", document));
		assertEquals("string", PropertyResolver.getValue("string", document));
		assertEquals("string2", PropertyResolver.getValue("nested.string", document));
	}

	class CustomGetAndSetLocator implements IPropertyLocator {

		private IPropertyLocator locator = new DefaultPropertyLocator();
		
		@Override
		public IGetAndSet get(Class<?> clz, String exp) {
			// first try default properties
			IGetAndSet getAndSet = locator.get(clz, exp);
			if (getAndSet == null && Document.class.isAssignableFrom(clz)) {
				// fall back to document properties
				getAndSet = new DocumentPropertyGetAndSet(exp);
			}
			return getAndSet;
		}

		public class DocumentPropertyGetAndSet extends AbstractGetAndSet {

			private String name;

			public DocumentPropertyGetAndSet(String name) {
				this.name = name;
			}

			@Override
			public Object getValue(Object object) {
				return ((Document) object).getProperty(name);
			}

			@Override
			public Object newValue(Object object) {
				return new Document();
			}

			@Override
			public void setValue(Object object, Object value, PropertyResolverConverter converter) {
				((Document) object).setProperty(name, value);
			}
		}
	}
}
