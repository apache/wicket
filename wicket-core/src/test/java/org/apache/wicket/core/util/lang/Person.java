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
package org.apache.wicket.core.util.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jcompagner
 * 
 */
public class Person
{
	private CharSequence name;

	private Address address;

	/** */
	public Address address2;

	private Address privateAddress;

	private Country country;

	private Map<String, Address> addressMap;

	private List<Address> addressList;

	private Address[] addressArray;

	private int age;


	private int onlyGetterPrimitive;

	private String onlyGetterString;

	/**
	 * @return test
	 */
	public int getOnlyGetterPrimitive()
	{
		return onlyGetterPrimitive;
	}

	/**
	 * @return test
	 */
	public String getOnlyGetterString()
	{
		return onlyGetterString;
	}

	/**
	 * @return The name
	 */
	public CharSequence getName()
	{
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(CharSequence name)
	{
		this.name = name;
	}

	/**
	 * @return The address
	 */
	public Address getAddress()
	{
		return address;
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
	public List<Address> getAddressList()
	{
		return addressList;
	}

	/**
	 * @param addressList
	 */
	public void setAddressList(List<Address> addressList)
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
			addressList = new ArrayList<Address>();
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
		return addressList.get(index);
	}

	/**
	 * @return addresses
	 */
	public Map<String, Address> getAddressMap()
	{
		return addressMap;
	}

	/**
	 * @param addressMap
	 */
	public void setAddressMap(Map<String, Address> addressMap)
	{
		this.addressMap = addressMap;
	}

	/**
	 * @return country
	 */
	public Country getCountry()
	{
		return country;
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
		return addressArray;
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
