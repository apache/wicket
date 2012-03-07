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
package org.apache.wicket.examples.compref;

import org.apache.wicket.util.io.IClusterable;

/**
 * An address.
 */
public class Address implements IClusterable
{
	private String address;
	private String postcode;
	private String city;
	private String country;

	/**
	 * Construct.
	 */
	public Address()
	{
	}

	/**
	 * Gets the address.
	 * 
	 * @return address
	 */
	public String getAddress()
	{
		return address;
	}

	/**
	 * Sets the address.
	 * 
	 * @param address
	 *            address
	 */
	public void setAddress(String address)
	{
		this.address = address;
	}

	/**
	 * Gets the city.
	 * 
	 * @return city
	 */
	public String getCity()
	{
		return city;
	}

	/**
	 * Sets the city.
	 * 
	 * @param city
	 *            city
	 */
	public void setCity(String city)
	{
		this.city = city;
	}

	/**
	 * Gets the country.
	 * 
	 * @return country
	 */
	public String getCountry()
	{
		return country;
	}

	/**
	 * Sets the country.
	 * 
	 * @param country
	 *            country
	 */
	public void setCountry(String country)
	{
		this.country = country;
	}

	/**
	 * Gets the postcode.
	 * 
	 * @return postcode
	 */
	public String getPostcode()
	{
		return postcode;
	}

	/**
	 * Sets the postcode.
	 * 
	 * @param postcode
	 *            postcode
	 */
	public void setPostcode(String postcode)
	{
		this.postcode = postcode;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[Address address=" + address + ", postcode=" + postcode + ", city=" + city +
				", country=" + country + "]";
	}
}
