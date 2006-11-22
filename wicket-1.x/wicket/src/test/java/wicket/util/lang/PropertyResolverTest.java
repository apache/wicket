/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * 
 */
package wicket.util.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;
import wicket.WicketRuntimeException;
import wicket.util.convert.ConversionException;
import wicket.util.convert.Converter;

/**
 * @author jcompagner
 *
 */
public class PropertyResolverTest extends TestCase
{
	private static final Converter CONVERTER = new Converter(Locale.US);
	
	private Person person;
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		person = new Person();
	}
	/**
	 * @throws Exception
	 */
	public void testSimpleExpression() throws Exception
	{
		String name = (String)PropertyResolver.getValue("name", person);
		assertNull(name);
		
		PropertyResolver.setValue("name", person, "wicket", CONVERTER);
		name = (String)PropertyResolver.getValue("name", person);
		assertEquals(name, "wicket");
	}
	
	/**
	 * @throws Exception
	 */
	public void testPrimitiveValue() throws Exception
	{
		Integer integer = (Integer)PropertyResolver.getValue("age", person);
		assertTrue(integer.intValue() == 0);
		
		PropertyResolver.setValue("age", person, new Integer(10), CONVERTER);
		integer = (Integer)PropertyResolver.getValue("age", person);
		assertTrue(integer.intValue() == 10);

		try
		{
			PropertyResolver.setValue("age", person, null, CONVERTER);
			fail("primitive type can't be set to null");
		} catch(ConversionException ce)
		{
			// ignore should happen
		}
	}

	/**
	 * @throws Exception
	 */
	public void testPathExpression() throws Exception
	{
		person.setAddress(new Address());
		PropertyResolver.setValue("address.street", person, "wicket-street",CONVERTER);
		String street = (String)PropertyResolver.getValue("address.street", person);
		assertEquals(street, "wicket-street");
		
	}

	/**
	 * @throws Exception
	 */
	public void testNull() throws Exception
	{
		String street = (String)PropertyResolver.getValue("address.street", person);
		assertNull(street);
	}
	
	/**
	 * @throws Exception
	 */
	public void testNullCreation() throws Exception
	{
		PropertyResolver.setValue("address.street", person, "wicket-street",CONVERTER);
		String street = (String)PropertyResolver.getValue("address.street", person);
		assertEquals(street, "wicket-street");

		try
		{
			PropertyResolver.setValue("country.name", person, "US",CONVERTER);
			throw new Exception("name can't be set on a country that doesn't have default constructor");
		}
		catch (WicketRuntimeException ex)
		{
		}
	}
	
	/**
	 * @throws Exception
	 */
	public void testGetterOnly() throws Exception
	{
		PropertyResolver.setValue("country", person, new Country("US"),CONVERTER);
		PropertyResolver.getValue("country.name", person);
		
		try
		{
			PropertyResolver.setValue("country.name", person, "NL",CONVERTER);
		}
		catch (WicketRuntimeException ex)
		{
		}
	}

	/**
	 * @throws Exception
	 */
	public void testPathExpressionWithConversion() throws Exception
	{
		person.setAddress(new Address());
		PropertyResolver.setValue("address.number", person, "10",CONVERTER);
		Integer number = (Integer)PropertyResolver.getValue("address.number", person);
		assertEquals(number, new Integer(10));
		
		try
		{
			PropertyResolver.setValue("address.number", person, "10a",CONVERTER);
			throw new Exception("Conversion error should be thrown");
		}
		catch (ConversionException ex)
		{
		}
		
	}
	
	/**
	 * @throws Exception
	 */
	public void testMapLookup() throws Exception
	{
		Address address = new Address();
		PropertyResolver.setValue("addressMap", person, new HashMap(), CONVERTER);
		PropertyResolver.setValue("addressMap.address", person, address, CONVERTER);
		PropertyResolver.setValue("addressMap.address.street", person, "wicket-street", CONVERTER);
		String street = (String)PropertyResolver.getValue("addressMap.address.street", person);
		assertEquals(street, "wicket-street");
	}
	
	/**
	 * @throws Exception
	 */
	public void testListLookup() throws Exception
	{
		PropertyResolver.setValue("addressList", person, new ArrayList(), CONVERTER);
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
	public void testArrayLookup() throws Exception
	{
		PropertyResolver.setValue("addressArray", person, new Address[] {new Address(),null}, CONVERTER);
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
	public void testArrayLookupByBrackets() throws Exception
	{
		PropertyResolver.setValue("addressArray", person, new Address[] {new Address(),null}, CONVERTER);
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
	public void testPropertyByIndexLookup() throws Exception
	{
		PropertyResolver.setValue("addressAt.0", person, new Address(), CONVERTER);
		PropertyResolver.setValue("addressAt.0.street", person, "wicket-street", CONVERTER);
		String street = (String)PropertyResolver.getValue("addressAt.0.street", person);
		assertEquals(street, "wicket-street");
	}
	
	/**
	 * @throws Exception 
	 */
	public void testListSizeLookup() throws Exception
	{
		List addresses = new ArrayList();
		addresses.add(new Address());
		addresses.add(new Address());
		person.setAddressList(addresses);
		Object size = PropertyResolver.getValue("addressList.size", person);
		assertEquals(size, new Integer(2));
		size = PropertyResolver.getValue("addressList.size()", person);
		assertEquals(size, new Integer(2));
	}
	
	/**
	 * @throws Exception 
	 */
	public void testMapSizeLookup() throws Exception
	{
		Map addresses = new HashMap();
		Address address = new Address();
		addresses.put("size",address);
		addresses.put("test",new Address());
		person.setAddressMap(addresses);
		Object addressFromMap = PropertyResolver.getValue("addressMap.size", person);
		assertEquals(addressFromMap, address);
		Object size = PropertyResolver.getValue("addressMap.size()", person);
		assertEquals(size, new Integer(2));
	}
	
	/**
	 * @throws Exception 
	 */
	public void testArraytSizeLookup() throws Exception
	{
		person.setAddressArray(new Address[] {new Address(), new Address()});
		Object size = PropertyResolver.getValue("addressArray.length", person);
		assertEquals(size, new Integer(2));
		size = PropertyResolver.getValue("addressArray.size", person);
		assertEquals(size, new Integer(2));
	}
	
	/**
	 * @throws Exception 
	 */
	public void testMethodLookup() throws Exception
	{
		Address[] addresses = new Address[] {new Address(), new Address()};
		person.setAddressArray(addresses);
		Object value = PropertyResolver.getValue("getAddressArray()", person);
		assertEquals(value, addresses);
	}
	
	/**
	 * @throws Exception
	 */
	public void testField() throws Exception
	{
		Address address = new Address();
		PropertyResolver.setValue("address2", person, address , CONVERTER);
		Address address2 = (Address)PropertyResolver.getValue("address2", person);
		assertEquals(address, address2);
		
		try
		{
			PropertyResolver.setValue("address3", person, address , CONVERTER);
			throw new RuntimeException("Shoudln't come here");
		}
		catch (RuntimeException ex)
		{
			
		}
	}
	
	/**
	 * @throws Exception
	 */
	public void testPrivateField() throws Exception
	{
		Address address = new Address();
		PropertyResolver.setValue("privateAddress", person, address , CONVERTER);
		Address address2 = (Address)PropertyResolver.getValue("privateAddress", person);
		assertEquals(address, address2);
	}	
	
	/**
	 * @throws Exception
	 */
	public void testPrivateFieldOfSuperClass() throws Exception
	{
		Person2 person2 = new Person2();
		Address address = new Address();
		PropertyResolver.setValue("privateAddress", person2, address , CONVERTER);
		Address address2 = (Address)PropertyResolver.getValue("privateAddress", person2);
		assertEquals(address, address2);
	}		
	
}
