/*
 * $Id$ $Revision$ $Date$
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
import java.util.List;
import java.util.Map;

/**
 * @author jcompagner
 * 
 */
public class Person
{
	private String name;

	private Address address;

	/** */
	public Address address2;
	
	private Address privateAddress;
	
	private Country country;

	private Map addressMap;

	private List addressList;

	private Address[] addressArray;
	
	private int age;

	/**
	 * @return The name
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return The address
	 */
	public Address getAddress()
	{
		return this.address;
	}

	/**
	 * @param address
	 */
	public void setAddress(Address address)
	{
		this.address = address;
	}

	/**
	 * @return addresses
	 */
	public List getAddressList()
	{
		return this.addressList;
	}

	/**
	 * @param addressList
	 */
	public void setAddressList(List addressList)
	{
		this.addressList = addressList;
	}

	/**
	 * @param index
	 * @param address
	 */
	public void setAddressAt(int index, Address address)
	{
		if (addressList == null)
		{
			addressList = new ArrayList();
		}
		while (addressList.size() < index)
		{
			addressList.add(null);
		}
		addressList.add(address);
	}

	/**
	 * @param index
	 * @return address
	 */
	public Address getAddressAt(int index)
	{
		return (Address)addressList.get(index);
	}

	/**
	 * @return addresses
	 */
	public Map getAddressMap()
	{
		return this.addressMap;
	}

	/**
	 * @param addressMap
	 */
	public void setAddressMap(Map addressMap)
	{
		this.addressMap = addressMap;
	}

	/**
	 * @return country
	 */
	public Country getCountry()
	{
		return this.country;
	}

	/**
	 * @param country
	 */
	public void setCountry(Country country)
	{
		this.country = country;
	}

	/**
	 * @return addresses
	 */
	public Address[] getAddressArray()
	{
		return this.addressArray;
	}

	/**
	 * @param addressArray
	 */
	public void setAddressArray(Address[] addressArray)
	{
		this.addressArray = addressArray;
	}

	/**
	 * @return The age of the person
	 */
	public int getAge()
	{
		return age;
	}

	/**
	 * @param age
	 */
	public void setAge(int age)
	{
		this.age = age;
	}
}
