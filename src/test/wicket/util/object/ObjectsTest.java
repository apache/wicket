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
package wicket.util.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import wicket.WicketRuntimeException;
import wicket.util.convert.ConversionException;
import wicket.util.convert.Converter;
import junit.framework.TestCase;

/**
 * @author jcompagner
 *
 */
public class ObjectsTest extends TestCase
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
		String name = (String)Objects.getValue("name", person);
		assertNull(name);
		
		Objects.setValue("name", person, "wicket", CONVERTER);
		name = (String)Objects.getValue("name", person);
		assertEquals(name, "wicket");
	}

	/**
	 * @throws Exception
	 */
	public void testPathExpression() throws Exception
	{
		person.setAddress(new Address());
		Objects.setValue("address.street", person, "wicket-street",CONVERTER);
		String street = (String)Objects.getValue("address.street", person);
		assertEquals(street, "wicket-street");
		
	}

	/**
	 * @throws Exception
	 */
	public void testNull() throws Exception
	{
		String street = (String)Objects.getValue("address.street", person);
		assertNull(street);
	}
	
	/**
	 * @throws Exception
	 */
	public void testNullCreation() throws Exception
	{
		Objects.setValue("address.street", person, "wicket-street",CONVERTER);
		String street = (String)Objects.getValue("address.street", person);
		assertEquals(street, "wicket-street");

		try
		{
			Objects.setValue("country.name", person, "US",CONVERTER);
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
		Objects.setValue("country", person, new Country("US"),CONVERTER);
		Objects.getValue("country.name", person);
		
		try
		{
			Objects.setValue("country.name", person, "NL",CONVERTER);
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
		Objects.setValue("address.number", person, "10",CONVERTER);
		Integer number = (Integer)Objects.getValue("address.number", person);
		assertEquals(number, new Integer(10));
		
		try
		{
			Objects.setValue("address.number", person, "10a",CONVERTER);
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
		Objects.setValue("addressMap", person, new HashMap(), CONVERTER);
		Objects.setValue("addressMap.address", person, address, CONVERTER);
		Objects.setValue("addressMap.address.street", person, "wicket-street", CONVERTER);
		String street = (String)Objects.getValue("addressMap.address.street", person);
		assertEquals(street, "wicket-street");
	}
	
	/**
	 * @throws Exception
	 */
	public void testListLookup() throws Exception
	{
		Objects.setValue("addressList", person, new ArrayList(), CONVERTER);
		Objects.setValue("addressList.0", person, new Address(), CONVERTER);
		Objects.setValue("addressList.10", person, new Address(), CONVERTER);
		Objects.setValue("addressList.1", person, new Address(), CONVERTER);
		Objects.setValue("addressList.1.street", person, "wicket-street", CONVERTER);
		
		String street = (String)Objects.getValue("addressList.0.street", person);
		assertNull(street);
		street = (String)Objects.getValue("addressList.1.street", person);
		assertEquals(street, "wicket-street");
	}
	
	/**
	 * @throws Exception
	 */
	public void testArrayLookup() throws Exception
	{
		Objects.setValue("addressArray", person, new Address[] {new Address(),null}, CONVERTER);
		Objects.setValue("addressArray.0.street", person, "wicket-street", CONVERTER);
		String street = (String)Objects.getValue("addressArray.0.street", person);
		assertEquals(street, "wicket-street");

		Objects.setValue("addressArray.1.street", person, "wicket-street", CONVERTER);
		street = (String)Objects.getValue("addressArray.1.street", person);
		assertEquals(street, "wicket-street");
	}	

	/**
	 * @throws Exception
	 */
	public void testArrayLookupByBrackets() throws Exception
	{
		Objects.setValue("addressArray", person, new Address[] {new Address(),null}, CONVERTER);
		Objects.setValue("addressArray[0].street", person, "wicket-street", CONVERTER);
		String street = (String)Objects.getValue("addressArray[0].street", person);
		assertEquals(street, "wicket-street");

		Objects.setValue("addressArray[1].street", person, "wicket-street", CONVERTER);
		street = (String)Objects.getValue("addressArray[1].street", person);
		assertEquals(street, "wicket-street");
	}	
	
	/**
	 * @throws Exception
	 */
	public void testPropertyByIndexLookup() throws Exception
	{
		Objects.setValue("addressAt.0", person, new Address(), CONVERTER);
		Objects.setValue("addressAt.0.street", person, "wicket-street", CONVERTER);
		String street = (String)Objects.getValue("addressAt.0.street", person);
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
		Object size = Objects.getValue("addressList.size", person);
		assertEquals(size, new Integer(2));
		size = (Integer)Objects.getValue("addressList.size()", person);
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
		Object addressFromMap = Objects.getValue("addressMap.size", person);
		assertEquals(addressFromMap, address);
		Object size = (Integer)Objects.getValue("addressMap.size()", person);
		assertEquals(size, new Integer(2));
	}
	
	/**
	 * @throws Exception 
	 */
	public void testArraytSizeLookup() throws Exception
	{
		person.setAddressArray(new Address[] {new Address(), new Address()});
		Object size = Objects.getValue("addressArray.length", person);
		assertEquals(size, new Integer(2));
		size = (Integer)Objects.getValue("addressArray.size", person);
		assertEquals(size, new Integer(2));
	}
	
	/**
	 * @throws Exception 
	 */
	public void testMethodLookup() throws Exception
	{
		Address[] addresses = new Address[] {new Address(), new Address()};
		person.setAddressArray(addresses);
		Object value = Objects.getValue("getAddressArray()", person);
		assertEquals(value, addresses);
	}
	
	/**
	 * @throws Exception
	 */
	public void testField() throws Exception
	{
		Address address = new Address();
		Objects.setValue("address2", person, address , CONVERTER);
		Address address2 = (Address)Objects.getValue("address2", person);
		assertEquals(address, address2);
		
		try
		{
			Objects.setValue("address3", person, address , CONVERTER);
			throw new RuntimeException("Shoudln't come here");
		}
		catch (RuntimeException ex)
		{
			
		}
	}
	
}
