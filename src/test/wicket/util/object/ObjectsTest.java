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
	/**
	 * @throws Exception
	 */
	public void testSimpleExpression() throws Exception
	{
		Person person = new Person();
		String name = (String)Objects.getValue("name", person);
		assertNull(name);
		
		Objects.setValue("name", person, "wicket", new Converter(Locale.US));
		name = (String)Objects.getValue("name", person);
		assertEquals(name, "wicket");
	}

	/**
	 * @throws Exception
	 */
	public void testPathExpression() throws Exception
	{
		Person person = new Person();
		person.setAddress(new Address());
		Objects.setValue("address.street", person, "wicket-street",new Converter(Locale.US));
		String street = (String)Objects.getValue("address.street", person);
		assertEquals(street, "wicket-street");
		
	}

	/**
	 * @throws Exception
	 */
	public void testNull() throws Exception
	{
		Person person = new Person();
		String street = (String)Objects.getValue("address.street", person);
		assertNull(street);
	}
	
	/**
	 * @throws Exception
	 */
	public void testNullCreation() throws Exception
	{
		Person person = new Person();
		Objects.setValue("address.street", person, "wicket-street",new Converter(Locale.US));
		String street = (String)Objects.getValue("address.street", person);
		assertEquals(street, "wicket-street");

		try
		{
			Objects.setValue("country.name", person, "US",new Converter(Locale.US));
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
		Person person = new Person();
		Objects.setValue("country", person, new Country("US"),new Converter(Locale.US));
		Objects.getValue("country.name", person);
		
		try
		{
			Objects.setValue("country.name", person, "NL",new Converter(Locale.US));
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
		Person person = new Person();
		person.setAddress(new Address());
		Objects.setValue("address.number", person, "10",new Converter(Locale.US));
		Integer number = (Integer)Objects.getValue("address.number", person);
		assertEquals(number, new Integer(10));
		
		try
		{
			Objects.setValue("address.number", person, "10a",new Converter(Locale.US));
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
		Person person = new Person();
		Address address = new Address();
		Objects.setValue("addressMap", person, new HashMap(), new Converter(Locale.US));
		Objects.setValue("addressMap.address", person, address, new Converter(Locale.US));
		Objects.setValue("addressMap.address.street", person, "wicket-street", new Converter(Locale.US));
		String street = (String)Objects.getValue("addressMap.address.street", person);
		assertEquals(street, "wicket-street");
	}
	
	/**
	 * @throws Exception
	 */
	public void testListLookup() throws Exception
	{
		Person person = new Person();
		Objects.setValue("addressList", person, new ArrayList(), new Converter(Locale.US));
		Objects.setValue("addressList.0", person, new Address(), new Converter(Locale.US));
		Objects.setValue("addressList.10", person, new Address(), new Converter(Locale.US));
		Objects.setValue("addressList.1", person, new Address(), new Converter(Locale.US));
		Objects.setValue("addressList.1.street", person, "wicket-street", new Converter(Locale.US));
		
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
		Person person = new Person();
		Objects.setValue("addressArray", person, new Address[] {new Address(),null}, new Converter(Locale.US));
		Objects.setValue("addressArray.0.street", person, "wicket-street", new Converter(Locale.US));
		String street = (String)Objects.getValue("addressArray.0.street", person);
		assertEquals(street, "wicket-street");

		Objects.setValue("addressArray.1.street", person, "wicket-street", new Converter(Locale.US));
		street = (String)Objects.getValue("addressArray.1.street", person);
		assertEquals(street, "wicket-street");
	}	

	/**
	 * @throws Exception
	 */
	public void testArrayLookupByBrackets() throws Exception
	{
		Person person = new Person();
		Objects.setValue("addressArray", person, new Address[] {new Address(),null}, new Converter(Locale.US));
		Objects.setValue("addressArray[0].street", person, "wicket-street", new Converter(Locale.US));
		String street = (String)Objects.getValue("addressArray[0].street", person);
		assertEquals(street, "wicket-street");

		Objects.setValue("addressArray[1].street", person, "wicket-street", new Converter(Locale.US));
		street = (String)Objects.getValue("addressArray[1].street", person);
		assertEquals(street, "wicket-street");
	}	
	
	/**
	 * @throws Exception
	 */
	public void testPropertyByIndexLookup() throws Exception
	{
		Person person = new Person();
		Objects.setValue("addressAt.0", person, new Address(), new Converter(Locale.US));
		Objects.setValue("addressAt.0.street", person, "wicket-street", new Converter(Locale.US));
		String street = (String)Objects.getValue("addressAt.0.street", person);
		assertEquals(street, "wicket-street");
	}
	
	/**
	 * @throws Exception 
	 */
	public void testListSizeLookup() throws Exception
	{
		Person person = new Person();
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
		Person person = new Person();
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
		Person person = new Person();
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
		Person person = new Person();
		Address[] addresses = new Address[] {new Address(), new Address()};
		person.setAddressArray(addresses);
		Object value = Objects.getValue("getAddressArray()", person);
		assertEquals(value, addresses);
	}	
	
}
